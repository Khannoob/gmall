package edu.sysu.gmall.index.service;

import edu.sysu.gmall.index.feign.GmallPmsClient;
import edu.sysu.gmall.pms.api.GmallPmsApi;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-24 18:26
 */
@Service
public class IndexService {

    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    RedisTemplate redisTemplate;

    public List<CategoryEntity> queryL1Categories() {
        return gmallPmsClient.queryCategoriesByPid(0l).getData();
    }

    private static final String KEY_PREFIX = "index:category:";

    public List<CategoryEntity> queryL2Categories(Long pid) {
        Object o = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (o != null) {
            return (List<CategoryEntity>) o;
        }
        List<CategoryEntity> categoryEntities = gmallPmsClient.queryL2CategoriesByPid(pid).getData();
        if (CollectionUtils.isEmpty(categoryEntities)) {
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, categoryEntities, 5, TimeUnit.MINUTES);
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + pid, categoryEntities, 60 + RandomUtils.nextInt(1, 30), TimeUnit.DAYS);
        return categoryEntities;

    }

    /**
     * 通过setnx实现简单的分布式分布式锁
     * 1.防止死锁
     *  a.比如客户端拿到锁之后马上发生宕机导致锁不能删除
     *                              ---> 设置锁的过期时间为3秒自动放锁(使用ifAbsent()重载方法保证其原子性)
     *  b.我们在a设置了过期时间3秒,但是如果有的业务超时比如7秒后才执行完,这时下一次业务可能已经提前删掉本次锁
     *                              ---> 解铃还须系铃人，删除之前要判断是否是自己的锁(UUID唯一value);并且判断和删除要有原子性(使用lua脚本)
     */
    private static final String SCRIPT = "if (redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
    public void testLock() {
        //1.加分布式锁setnx 并设置过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean absent = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (!absent) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
            testLock();
            //一定要让重试方法出栈
            return;
        }
        //2.执行业务
        Integer num = (Integer) redisTemplate.opsForValue().get("key");
        if (num == null) {
            redisTemplate.opsForValue().set("key", 1);
        } else {
            redisTemplate.opsForValue().set("key", ++num);
        }
        //3.释放分布式锁 先判断value与设置的value(UUID)是否一致 并且判断和删除要有原子性 lua脚本

        redisTemplate.execute(new DefaultRedisScript<>(SCRIPT,Boolean.class), Arrays.asList("lock"), uuid);

/*(2)        String value = (String)redisTemplate.opsForValue().get("lock");
        if (StringUtils.equals(value, uuid)){
            redisTemplate.delete("lock");
        }*/


//(1)        redisTemplate.delete("lock");
    }
}

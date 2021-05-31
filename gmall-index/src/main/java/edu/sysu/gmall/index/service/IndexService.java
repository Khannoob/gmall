package edu.sysu.gmall.index.service;

import edu.sysu.gmall.index.config.GmallCache;
import edu.sysu.gmall.index.feign.GmallPmsClient;
import edu.sysu.gmall.index.utils.DistributedLock;
import edu.sysu.gmall.pms.api.GmallPmsApi;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    DistributedLock distributedLock;
    @Autowired
    RedissonClient redissonClient;

    public List<CategoryEntity> queryL1Categories() {
        return gmallPmsClient.queryCategoriesByPid(0l).getData();
    }

    private static final String KEY_PREFIX = "index:category:";
    private static final String LOCK_PREFIX = "index:key:category:";
    @GmallCache(prefix = KEY_PREFIX,lock = LOCK_PREFIX)
    public List<CategoryEntity> queryL2Categories(Long pid) {
            List<CategoryEntity> categoryEntities = gmallPmsClient.queryL2CategoriesByPid(pid).getData();
        System.out.println(Thread.currentThread().getName()+"去调用了远程接口........");
            return categoryEntities;
    }
    public List<CategoryEntity> queryL2Categories2(Long pid) {
        Object o = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (o != null) {
            return (List<CategoryEntity>) o;
        }
        //为了防止缓存击穿,给缓存设置分布式锁redisson,防止热点key过期后大量请求直达数据库
        RLock fairLock = redissonClient.getFairLock(LOCK_PREFIX + pid);
        try {
            fairLock.lock();
            //再次判断是否缓存已有数据
            Object o1 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
            if (o1 != null) {
                return (List<CategoryEntity>) o1;
            }
            System.out.println(Thread.currentThread().getName() + "去远程调用操纵了数据库............");
            List<CategoryEntity> categoryEntities = gmallPmsClient.queryL2CategoriesByPid(pid).getData();
            if (CollectionUtils.isEmpty(categoryEntities)) {
                //为了缓存穿透,给为value=null的key也建立缓存........
                redisTemplate.opsForValue().set(KEY_PREFIX + pid, categoryEntities, 5, TimeUnit.MINUTES);
                return null;
            }
            //为了防止缓存雪崩,给缓存的过期时间加上随机值
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, categoryEntities, 60 + RandomUtils.nextInt(1, 30), TimeUnit.DAYS);
            return categoryEntities;
        } finally {
            fairLock.unlock();
        }
    }

    /**
     * 通过setnx实现简单的分布式分布式锁
     * 1.防止死锁
     * a.比如客户端拿到锁之后马上发生宕机导致锁不能删除
     * ---> 设置锁的过期时间为3秒自动放锁(使用ifAbsent()重载方法保证其原子性)
     * b.我们在a设置了过期时间3秒,但是如果有的业务超时比如7秒后才执行完,这时下一次业务可能已经提前删掉本次锁
     * ---> 解铃还须系铃人，删除之前要判断是否是自己的锁(UUID唯一value);并且判断和删除要有原子性(使用lua脚本)
     */

    private static final String SCRIPT = "if (redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";

    public void testLock2() {
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

        redisTemplate.execute(new DefaultRedisScript<>(SCRIPT, Boolean.class), Arrays.asList("lock"), uuid);

/*(2)        String value = (String)redisTemplate.opsForValue().get("lock");
        if (StringUtils.equals(value, uuid)){
            redisTemplate.delete("lock");
        }*/


//(1)        redisTemplate.delete("lock");
    }

    public void testLock3() {
        String uuid = UUID.randomUUID().toString();
        try {
            //1.加分布式锁 并设置过期时间
            Boolean flag = distributedLock.tryLock("lock", uuid, 30);
            //2.执行业务
//            TimeUnit.SECONDS.sleep(100);
            Integer num = (Integer) redisTemplate.opsForValue().get("key");
            if (num == null) {
                redisTemplate.opsForValue().set("key", 1);
            } else {
                redisTemplate.opsForValue().set("key", ++num);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3.释放分布式锁 先判断value与设置的value(UUID)是否一致 并且判断和删除要有原子性 lua脚本
            distributedLock.unLock("lock", uuid);
        }
    }

    public void testLock() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock(30, TimeUnit.SECONDS);
        Integer num = (Integer) redisTemplate.opsForValue().get("key");
        if (num == null) {
            redisTemplate.opsForValue().set("key", 1);
        } else {
            redisTemplate.opsForValue().set("key", ++num);
        }
        lock.unlock();
    }

    public String testReadLock() {
        RReadWriteLock rw = redissonClient.getReadWriteLock("rw");
        RLock readLock = rw.readLock();
        readLock.lock(10, TimeUnit.SECONDS);
        return "测试读锁........";
    }

    public String testWriteLock() {
        RReadWriteLock rw = redissonClient.getReadWriteLock("rw");
        RLock writeLock = rw.writeLock();
        writeLock.lock(10, TimeUnit.SECONDS);
        return "测试写锁........";
    }

    public void testSemaphore() {
//        semaphore.trySetPermits(3);
        RSemaphore semaphore = redissonClient.getSemaphore("carport");
        try {
            semaphore.acquire();
            System.out.println(Thread.currentThread().getId() + "车获取到了车位..........");
            TimeUnit.SECONDS.sleep(5);
            System.out.println(Thread.currentThread().getId() + "车开走了...........");
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testLatch() {
        RCountDownLatch cdl = redissonClient.getCountDownLatch("cdl");
        cdl.trySetCount(4);
        try {
            cdl.await();
            //具体的业务
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testCountDown() {
        RCountDownLatch cdl = redissonClient.getCountDownLatch("cdl");
        cdl.countDown();
        System.out.println(Thread.currentThread().getId() + "同学出来了.............");
    }
}

package edu.sysu.gmall.index.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-26 14:00
 */
@Component
public class DistributedLock {
    @Autowired
    RedisTemplate redisTemplate;
    private Timer timer;

    private final static String LOCK_SCRIPT = "if(redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',KEYS[1],ARGV[1])==1) then redis.call('hincrby',KEYS[1],ARGV[1],1) redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";
    private final static String UNLOCK_SCRIPT = "if(redis.call('hexists',KEYS[1],ARGV[1])==0) then return nil elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1)==0) then return redis.call('del',KEYS[1]) else return 0 end";
    private final static String RENEW_SCRIPT = "if(redis.call('hexists',KEYS[1],ARGV[1]) == 1) then redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";
    /**
     *  可重入锁的加锁方法
     * @param lockName 锁名
     * @param uuid 唯一value值
     * @param timeout 锁的过期时间(防止程序宕机导致死锁)
     * @return
     */
    public Boolean tryLock(String lockName, String uuid, Integer timeout) {
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript(LOCK_SCRIPT, Boolean.class), Arrays.asList(lockName), uuid, timeout);
        if (!flag) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
                this.tryLock(lockName, uuid, timeout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            this.renewLock(lockName,uuid,timeout);
        }
        return true;
    }

    /**
     * 可重入锁的解锁方法
     * @param lockName 锁名
     * @param uuid 验证是不是自己的锁
     */
    public void unLock(String lockName, String uuid) {
        Long flag = (Long)redisTemplate.execute(new DefaultRedisScript(UNLOCK_SCRIPT, Long.class), Arrays.asList(lockName), uuid);
        if (flag==null){
            throw new RuntimeException("你在恶意释放别人的锁!");
        }else if (flag==1){
            this.timer.cancel();
        }
    }

    private void renewLock(String lockName, String uuid, Integer timeout){
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
        redisTemplate.execute(new DefaultRedisScript(RENEW_SCRIPT, Boolean.class), Arrays.asList(lockName), uuid,timeout);
            }
        }, timeout*1000/3,timeout*1000/3);
    }
}

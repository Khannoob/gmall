package edu.sysu.gmall.index.config;

import edu.sysu.gmall.index.feign.GmallPmsClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-28 19:38
 */
@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    RBloomFilter rBloomFilter;

    /**
     * 环绕通知 对目标进行增强
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(edu.sysu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //方法参数 pid
        Object[] args = joinPoint.getArgs();
        //方法注解 GmallCache
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallCache = signature.getMethod().getDeclaredAnnotation(GmallCache.class);
        //查询redis的Key
        String key = gmallCache.prefix() + StringUtils.join(args, ",");
        //分布式锁的lock_key
        String lock = gmallCache.lock() + StringUtils.join(args, ",");
        //过期时间
        int timeout = gmallCache.timeout();
        //随机值边界
        int random = gmallCache.random();
        //TODO::通过布隆过滤器过滤掉无效请求 防止缓存穿透
        boolean contains = rBloomFilter.contains(args[0]);
        if (!contains) {
            return null;
        }
        //1.查询缓存 缓存命中直接返回
        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            return o;
        }
        //2.缓存没有->执行目标方法
        //2.1. 加上分布式锁 防止缓存击穿
        RLock fairLock = redissonClient.getFairLock(lock);
        fairLock.lock();
        try {
            //再次判断是否缓存已有数据
            Object o1 = redisTemplate.opsForValue().get(key);
            System.out.println("二重判断....."+o1);
            if (o1 != null) {
                return o1;
            }
            Object result = joinPoint.proceed(args);
            //3.把查到的数据放入Redis
            //3.1.1设置随机的过期时间防止缓存雪崩
            redisTemplate.opsForValue().set(key, result, timeout + new Random().nextInt(random), TimeUnit.MINUTES);
            return result;
        } finally {
            fairLock.unlock();
        }
    }
}

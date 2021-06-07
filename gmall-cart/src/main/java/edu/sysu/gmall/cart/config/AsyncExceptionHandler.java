package edu.sysu.gmall.cart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-03 10:48
 */
@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Autowired
    RedisTemplate redisTemplate;

    private static final String EXCEPTION_KEY = "cart:exception";

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("捕获到异步调用异常={},导致异常的方法={},方法参数={}", throwable, method, objects);
        //我们需要把异常上传到redis key=AsyncException Set{userId1,userId2,userId3}
        //userId是第一个参数 写mapper的时候所有mapper第一个参数都是userId
        // 把异常用户信息存入redis
        String userId = objects[0].toString();
//        BoundListOperations<String, String> listOps = this.redisTemplate.boundListOps(EXCEPTION_KEY);
//        listOps.leftPush(userId);

        BoundSetOperations setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);
        setOps.add(userId);
    }
}

package edu.sysu.gmall.cart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-03 10:48
 */
@Configuration
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("捕获到异步调用异常={},导致异常的方法={},方法参数={}", throwable, method, objects);
    }
}

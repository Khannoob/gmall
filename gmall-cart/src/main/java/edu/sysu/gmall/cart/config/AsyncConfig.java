package edu.sysu.gmall.cart.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-03 10:52
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Autowired
    AsyncExceptionHandler asyncExceptionHandler;

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncExceptionHandler;
    }
}

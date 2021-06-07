package edu.sysu.gmall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-31 13:08
 */
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor poolExecutor() {
        return new ThreadPoolExecutor(12,
                24, 30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(48));
    }
}

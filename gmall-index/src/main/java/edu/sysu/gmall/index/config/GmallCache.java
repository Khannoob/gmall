package edu.sysu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    /**
     * 缓存的前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的过期时间
     * 单位 min
     * @return
     */
    int timeout() default 30;

    /**
     * 防止缓存雪崩,给过期时间再加一个随机值
     * 这是随机值的最大边界
     * @return
     */
    int random() default 10;

    /**
     * 为了防止缓存击穿,给缓存添加分布式锁
     * 这里是分布式锁key的前缀
     * @return
     */
    String lock() default "lock";
}

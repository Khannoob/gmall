package edu.sysu.gmall.scheduled.jobhandler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import edu.sysu.gmall.cart.pojo.Cart;
import edu.sysu.gmall.scheduled.mapper.CartMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-04 18:49
 */
@Component
public class CartExceptionXxlHandler {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    CartMapper cartMapper;

    private static final String EXCEPTION_KEY = "cart:exception";
    private static final String KEY_PREFIX = "cart:info:";

    @XxlJob("test")
    public ReturnT<String> test(String param) {
        XxlJobLogger.log("使用XxlJobLogger打印执行日志，O(∩_∩)O");
        System.out.println("我的任务执行了：" + param + "，线程：" + Thread.currentThread().getName());
        return ReturnT.SUCCESS;
    }

    /**
     * 我们在新增某位用户的购物车时候可能保存到redis成功了,但是Async保存到mysql失败(原因可能还是mysql挂掉了)
     * 在cart的全局异常处理类,我们以cart:exception作为key userId作为value 上传到了redis
     * 我们在这里调用定时任务 10天执行一次userId对应所有的cart重新保存到mysql
     *
     * @return
     */
    @XxlJob("cartScheduleData")
    public ReturnT<String> cartScheduleData(String param) {
        XxlJobLogger.log("把redis内储存Async的错误信息还原到数据库，O(∩_∩)O");
        BoundSetOperations setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);

        Object userId = setOps.pop().toString();
        System.out.println(userId);
        while (userId != null) {
            userId = userId.toString();
            System.out.println(KEY_PREFIX + userId);
            //1.先把mysql userId对应的购物车记录全删
            cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));
            //2.把redis中该userId的购物车记录遍历重新插入到mysql
            BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List carts = hashOps.values();
            System.out.println("carts = " + carts);
            if (carts != null) {
                carts.forEach(o -> {
                    Cart cart = (Cart)o;
                    cartMapper.insert(cart);
                });
            }
            //3.吐出下一个下一个userId
            userId = setOps.pop();
        }

        return ReturnT.SUCCESS;
    }
}

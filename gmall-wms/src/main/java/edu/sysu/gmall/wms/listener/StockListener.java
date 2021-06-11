package edu.sysu.gmall.wms.listener;

import com.rabbitmq.client.Channel;
import edu.sysu.gmall.wms.mapper.WareSkuMapper;
import edu.sysu.gmall.wms.vo.SkuLockVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-07 20:39
 */
@Component
public class StockListener {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    WareSkuMapper wareSkuMapper;
    private static final String KEY_PREFIX = "stock:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("STOCK_UNLOCK_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"stock.unlock"}
    ))
    public void unlock(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //获取redis中的锁定数据 一定要redis中有数据才去解锁mysql的库存
        List<Object> data = (List) redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (CollectionUtils.isEmpty(data)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //解锁mysql中锁定的数据
        data.forEach(o -> {
            SkuLockVo skuLockVo = (SkuLockVo) o;
            Long wareSkuId = skuLockVo.getWareSkuId();
            Integer count = skuLockVo.getCount();
            wareSkuMapper.unlockWare(wareSkuId, count);
        });
        //删除redis的锁定数据
        redisTemplate.delete(KEY_PREFIX + orderToken);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("STOCK_MINUS_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"stock.minus"}
    ))
    public void minus(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //获取redis中的锁定数据 一定要redis中有数据才去减少mysql的库存
        List<Object> data = (List) redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (CollectionUtils.isEmpty(data)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //解锁mysql中锁定的数据
        data.forEach(o -> {
            SkuLockVo skuLockVo = (SkuLockVo) o;
            Long wareSkuId = skuLockVo.getWareSkuId();
            Integer count = skuLockVo.getCount();
            wareSkuMapper.minus(wareSkuId, count);
        });
        //删除redis的锁定数据
        redisTemplate.delete(KEY_PREFIX + orderToken);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

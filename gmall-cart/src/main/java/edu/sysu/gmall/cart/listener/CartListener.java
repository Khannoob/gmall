package edu.sysu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import edu.sysu.gmall.cart.feign.GmallPmsClient;
import edu.sysu.gmall.cart.mapper.CartMapper;
import edu.sysu.gmall.cart.pojo.Cart;
import edu.sysu.gmall.pms.entity.SkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-04 12:56
 */
@Component
public class CartListener {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    CartMapper cartMapper;
    private static final String PRICE_PREFIX = "cart:price:";
    private static final String KEY_PREFIX = "cart:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART_PRICE_QUEUE"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.update"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws Exception {
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        List<SkuEntity> skuEntities = gmallPmsClient.querySkusBySpu(spuId).getData();
        if (CollectionUtils.isEmpty(skuEntities)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        try {
            skuEntities.forEach(skuEntity -> {
                redisTemplate.opsForValue().set(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice());
            });
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if (message.getMessageProperties().isRedelivered()) {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART_DELETE_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"cart.delete"}
    ))
    public void delete(Map<String, Object> msg, Channel channel, Message message) throws Exception {
        if (CollectionUtils.isEmpty(msg)) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        String userId = msg.get("userId").toString();
        List<String> skuIds = JSON.parseArray(msg.get("skuIds").toString(), String.class);
        if (StringUtils.isBlank(userId) || CollectionUtils.isEmpty(skuIds)) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        hashOps.delete(skuIds.toArray());
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).in("sku_id", skuIds));
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    }
}

package edu.sysu.gmall.oms.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import edu.sysu.gmall.oms.entity.OrderEntity;
import edu.sysu.gmall.oms.mapper.OrderMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-07 20:22
 */
@Component
public class OrderListener {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;

    //标记无效订单的MQ
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ORDER_DISABLE_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"order.disable"}
    ))
    public void disable(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        orderMapper.updateStatus(orderToken, 0, 5);
        //解除库存的占用 mq发送消息给wms要求解锁库存
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unlock", orderToken);
    }

    //定时关单MQ 监听的是死信队列
    @RabbitListener(queues = "ORDER_DEAD_QUEUE")
    public void closeOrder(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        orderMapper.updateStatus(orderToken, 0, 4);
        //解除库存的占用 mq发送消息给wms要求解锁库存
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unlock", orderToken);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    //订单成功的MQ
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ORDER_SUCCESS_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"order.success"}
    ))
    public void success(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        orderMapper.updateStatus(orderToken, 0, 1);
        //解除库存的占用 mq发送消息给wms要求解锁库存
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.minus", orderToken);
    }
}

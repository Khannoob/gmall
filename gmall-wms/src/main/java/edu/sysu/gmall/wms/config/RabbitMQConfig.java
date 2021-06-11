package edu.sysu.gmall.wms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-24 19:11
 */
@Configuration
@Slf4j
public class RabbitMQConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息没有到达交换机,原因是:{}", cause);
            }
        });
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("消息没有到达队列,交换机:{},路由KEY:{},消息内容:{},失败原因:{}...."
                    , exchange, routingKey, new String(message.getBody()), replyText);
        });
    }

    //延时交换机 ORDER_EXCHANGE ✔
    //延时队列 ORDER_TTL_QUEUE 并且声明死信队列
    @Bean
    public Queue ttlQueue() {
        //要比关单长一点
        return QueueBuilder.durable("STOCK_TTL_QUEUE").ttl(100000)
                .deadLetterExchange("ORDER_EXCHANGE").deadLetterRoutingKey("stock.unlock").build();
    }

    //延时队列绑定到延时交换机 order.ttl
    @Bean
    public Binding ttlQueueBinding() {
        //这里因为exchange已经声明好了 所以不要使用BindingBuilder
        return new Binding("STOCK_TTL_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE", "stock.ttl", null);
    }

    //死信交换机 ORDER_EXCHANGE ✔
    //死信队列 STOCK_UNLOCK_QUEUE ✔
    //死信队列绑定到死信交换机 stock.unlock ✔
}

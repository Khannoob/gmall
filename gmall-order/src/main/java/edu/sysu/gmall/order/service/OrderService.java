package edu.sysu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import edu.sysu.gmall.cart.pojo.Cart;
import edu.sysu.gmall.common.exception.OrderException;
import edu.sysu.gmall.oms.vo.OrderItemVo;
import edu.sysu.gmall.oms.vo.OrderSubmitVo;
import edu.sysu.gmall.order.feign.*;
import edu.sysu.gmall.order.interceptor.LoginInterceptor;
import edu.sysu.gmall.order.vo.OrderConfirmVo;
import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;
import edu.sysu.gmall.pms.entity.SkuEntity;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import edu.sysu.gmall.ums.entity.UserAddressEntity;
import edu.sysu.gmall.ums.entity.UserEntity;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import edu.sysu.gmall.wms.vo.SkuLockVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-05 16:48
 */
@Service
public class OrderService {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    GmallCartClient cartClient;
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallSmsClient smsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GmallUmsClient umsClient;
    @Autowired
    GmallOmsClient omsClient;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        String userId = LoginInterceptor.getUserInfo().getUserId();

        //1.??????????????????
        CompletableFuture<List<Cart>> cartsFuture = CompletableFuture.supplyAsync(() -> {
            List<Cart> carts = cartClient.queryCheckedCartByUserId(userId).getData();
            return carts;
        }, threadPoolExecutor);

        CompletableFuture<Void> itemsFuture = cartsFuture.thenAcceptAsync((carts) -> {
            List<OrderItemVo> items = carts.stream().map(cart -> {
                BigDecimal count = cart.getCount();
                Long skuId = cart.getSkuId();
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(skuId);
                orderItemVo.setCount(count);

                CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
                    //???????????????pms-sku??????
                    SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();
                    orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                    orderItemVo.setPrice(skuEntity.getPrice());
                    orderItemVo.setWeight(skuEntity.getWeight());
                    orderItemVo.setTitle(skuEntity.getTitle());
                }, threadPoolExecutor);

                //???????????????pms-saleSkuAttr??????
                CompletableFuture<Void> saleSkuFuture = CompletableFuture.runAsync(() -> {
                    List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySkuAttrValueEntityListBySkuId(skuId).getData();
                    if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                        orderItemVo.setSaleAttrs(skuAttrValueEntities);
                    }
                }, threadPoolExecutor);

                //???????????????sms??????
                CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(() -> {
                    List<ItemSalesVo> itemSalesVos = smsClient.allSales(skuId).getData();
                    if (!CollectionUtils.isEmpty(itemSalesVos)) {
                        orderItemVo.setSales(itemSalesVos);
                    }
                }, threadPoolExecutor);

                //???????????????wms??????
                CompletableFuture<Void> wmsFuture = CompletableFuture.runAsync(() -> {
                    List<WareSkuEntity> wareSkuEntities = wmsClient.queryWareSkuBySid(skuId).getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                    }
                }, threadPoolExecutor);
                CompletableFuture.allOf(skuFuture, saleSkuFuture, smsFuture, wmsFuture).join();
                return orderItemVo;
            }).collect(Collectors.toList());
            confirmVo.setItems(items);
        }, threadPoolExecutor);


        //2.??????????????????????????????
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            List<UserAddressEntity> addresses = umsClient.queryUserAddressByUserId(userId).getData();
            confirmVo.setAddresses(addresses);
        }, threadPoolExecutor);


        //3.????????????????????????
        CompletableFuture<Void> boundsFuture = CompletableFuture.runAsync(() -> {
            UserEntity user = umsClient.queryUserById(Long.valueOf(userId)).getData();
            if (user != null) {
                confirmVo.setBounds(user.getIntegration());
            }
        }, threadPoolExecutor);

        //4.??????IdWorker?????????????????????  ??????????????????Redis ?????????submit??????????????? ???????????????????????????????????? ??????????????????redis????????? ?????????????????????
        CompletableFuture<Void> orderTokenFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getIdStr();
            confirmVo.setOrderToken(orderToken);
            redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken, 1, TimeUnit.HOURS);
        }, threadPoolExecutor);
        CompletableFuture.allOf(itemsFuture, addressFuture, boundsFuture, orderTokenFuture).join();

        return confirmVo;
    }

    public OrderConfirmVo confirmOrder2() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        String userId = LoginInterceptor.getUserInfo().getUserId();

        List<Cart> carts = cartClient.queryCheckedCartByUserId(userId).getData();
        //1.??????????????????
        List<OrderItemVo> items = carts.stream().map(cart -> {
            BigDecimal count = cart.getCount();
            Long skuId = cart.getSkuId();
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(skuId);
            orderItemVo.setCount(count);
            //???????????????pms-sku??????
            SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();
            orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
            orderItemVo.setPrice(skuEntity.getPrice());
            orderItemVo.setWeight(skuEntity.getWeight());
            orderItemVo.setTitle(skuEntity.getTitle());
            //???????????????pms-saleSkuAttr??????
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySkuAttrValueEntityListBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                orderItemVo.setSaleAttrs(skuAttrValueEntities);
            }
            //???????????????sms??????
            List<ItemSalesVo> itemSalesVos = smsClient.allSales(skuId).getData();
            if (!CollectionUtils.isEmpty(itemSalesVos)) {
                orderItemVo.setSales(itemSalesVos);
            }
            //???????????????wms??????
            List<WareSkuEntity> wareSkuEntities = wmsClient.queryWareSkuBySid(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            return orderItemVo;
        }).collect(Collectors.toList());
        confirmVo.setItems(items);

        //2.??????????????????????????????
        List<UserAddressEntity> addresses = umsClient.queryUserAddressByUserId(userId).getData();
        confirmVo.setAddresses(addresses);

        //3.????????????????????????
        UserEntity user = umsClient.queryUserById(Long.valueOf(userId)).getData();
        if (user != null) {
            confirmVo.setBounds(user.getIntegration());
        }
        //4.??????IdWorker?????????????????????  ??????????????????Redis ?????????submit??????????????? ???????????????????????????????????? ??????????????????redis????????? ?????????????????????
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);
        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);
        return confirmVo;
    }

    private static final String SCRIPT = "if (redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";

    public void submitOrder(OrderSubmitVo orderSubmitVo) {
        //1.??????orderToken???????????? ??????orderToken?????? (???????????????)
        String orderToken = orderSubmitVo.getOrderToken();
        //??????lua????????????orderToken???????????????
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript(SCRIPT, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if (!flag) {
            throw new OrderException("???????????????????????????????????????");
        }

        //2.?????? ????????????????????????????????????ok
        List<OrderItemVo> items = orderSubmitVo.getItems();
        BigDecimal totalPrice = orderSubmitVo.getTotalPrice();
        BigDecimal tPrice = items.stream().map(orderItemVo -> {
            SkuEntity skuEntity = pmsClient.querySkuById(orderItemVo.getSkuId()).getData();
            return skuEntity.getPrice().multiply(orderItemVo.getCount());
        }).reduce((a, b) -> a.add(b)).get();
        if (tPrice.compareTo(totalPrice) != 0) {
            throw new OrderException("???????????????????????????????????????");
        }

        //3.????????? ????????? (????????????)
        List<SkuLockVo> skuLockVos = items.stream().map((orderItemVo) -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setCount(orderItemVo.getCount().intValue());
            skuLockVo.setSkuId(orderItemVo.getSkuId());
            return skuLockVo;
        }).collect(Collectors.toList());
        List<SkuLockVo> lockVos = wmsClient.checkLock(orderToken, skuLockVos).getData();
        if (!CollectionUtils.isEmpty(lockVos)) {
            throw new OrderException(JSON.toJSONString(lockVos));
        }

        //4.????????????
        String userId = null;
        try {
            userId = LoginInterceptor.getUserInfo().getUserId();
            omsClient.saveOrder(orderSubmitVo, Long.valueOf(userId));
//            int i = 1 / 0;
        } catch (Exception e) {
            e.printStackTrace();
            //???????????????????????????????????????????????? ???????????????oms????????????????????????0(expect)???5(target) ????????????
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.disable", orderToken);
            throw new OrderException("???????????????!");
        }
//        int i = 1 / 0;
        //5.??????mq ???????????????????????????(??????????????????) ????????????skuIds???userId
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("skuIds", JSON.toJSON(skuIds));
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "cart.delete", msg);

        //6.??????mq???????????? ???????????? 90s????????????????????? ???????????????????????? oms??????????????? 0(expect)???4(target)
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.ttl", orderToken);
    }
}

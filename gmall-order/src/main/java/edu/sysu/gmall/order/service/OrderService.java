package edu.sysu.gmall.order.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import edu.sysu.gmall.cart.pojo.Cart;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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
    ThreadPoolExecutor threadPoolExecutor;
    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        String userId = LoginInterceptor.getUserInfo().getUserId();

        //1.查询商品清单
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
                    //设置商品的pms-sku属性
                    SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();
                    orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                    orderItemVo.setPrice(skuEntity.getPrice());
                    orderItemVo.setWeight(skuEntity.getWeight());
                    orderItemVo.setTitle(skuEntity.getTitle());
                }, threadPoolExecutor);

                //设置商品的pms-saleSkuAttr属性
                CompletableFuture<Void> saleSkuFuture = CompletableFuture.runAsync(() -> {
                    List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySkuAttrValueEntityListBySkuId(skuId).getData();
                    if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                        orderItemVo.setSaleAttrs(skuAttrValueEntities);
                    }
                }, threadPoolExecutor);

                //设置商品的sms属性
                CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(() -> {
                    List<ItemSalesVo> itemSalesVos = smsClient.allSales(skuId).getData();
                    if (!CollectionUtils.isEmpty(itemSalesVos)) {
                        orderItemVo.setSales(itemSalesVos);
                    }
                }, threadPoolExecutor);

                //设置商品的wms属性
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


        //2.查询收货人的地址信息
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            List<UserAddressEntity> addresses = umsClient.queryUserAddressByUserId(userId).getData();
            confirmVo.setAddresses(addresses);
        }, threadPoolExecutor);


        //3.查询收货人的积分
        CompletableFuture<Void> boundsFuture = CompletableFuture.runAsync(() -> {
            UserEntity user = umsClient.queryUserById(Long.valueOf(userId)).getData();
            if (user != null) {
                confirmVo.setBounds(user.getIntegration());
            }
        }, threadPoolExecutor);

        //4.使用IdWorker生成订单防重码  并把订单放入Redis 每一次submit订单都校验 第一次提交订单后删除此码 后续重复提交redis中无码 保证订单幂等性
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
        //1.查询商品清单
        List<OrderItemVo> items = carts.stream().map(cart -> {
            BigDecimal count = cart.getCount();
            Long skuId = cart.getSkuId();
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(skuId);
            orderItemVo.setCount(count);
            //设置商品的pms-sku属性
            SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();
            orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
            orderItemVo.setPrice(skuEntity.getPrice());
            orderItemVo.setWeight(skuEntity.getWeight());
            orderItemVo.setTitle(skuEntity.getTitle());
            //设置商品的pms-saleSkuAttr属性
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySkuAttrValueEntityListBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                orderItemVo.setSaleAttrs(skuAttrValueEntities);
            }
            //设置商品的sms属性
            List<ItemSalesVo> itemSalesVos = smsClient.allSales(skuId).getData();
            if (!CollectionUtils.isEmpty(itemSalesVos)) {
                orderItemVo.setSales(itemSalesVos);
            }
            //设置商品的wms属性
            List<WareSkuEntity> wareSkuEntities = wmsClient.queryWareSkuBySid(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            return orderItemVo;
        }).collect(Collectors.toList());
        confirmVo.setItems(items);

        //2.查询收货人的地址信息
        List<UserAddressEntity> addresses = umsClient.queryUserAddressByUserId(userId).getData();
        confirmVo.setAddresses(addresses);

        //3.查询收货人的积分
        UserEntity user = umsClient.queryUserById(Long.valueOf(userId)).getData();
        if (user != null) {
            confirmVo.setBounds(user.getIntegration());
        }
        //4.使用IdWorker生成订单防重码  并把订单放入Redis 每一次submit订单都校验 第一次提交订单后删除此码 后续重复提交redis中无码 保证订单幂等性
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);
        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);
        return confirmVo;
    }

    public void submitOrder(OrderSubmitVo orderSubmitVo) {
        //1.校验orderToken是否重复 删除orderToken (接口幂等性)
        //2.验价 总价相同就ok
        //3.查库存 锁库存 (分布式锁)
        //4.创建订单
        //5.异步删除购物车数据
    }
}

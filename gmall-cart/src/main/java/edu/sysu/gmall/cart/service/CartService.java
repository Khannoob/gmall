package edu.sysu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import edu.sysu.gmall.common.exception.CartException;
import edu.sysu.gmall.cart.feign.GmallPmsClient;
import edu.sysu.gmall.cart.feign.GmallSmsClient;
import edu.sysu.gmall.cart.feign.GmallWmsClient;
import edu.sysu.gmall.cart.interceptor.LoginInterceptor;
import edu.sysu.gmall.cart.pojo.Cart;
import edu.sysu.gmall.cart.pojo.UserInfo;
import edu.sysu.gmall.common.exception.OrderException;
import edu.sysu.gmall.pms.entity.SkuEntity;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-02 15:15
 */
@Service
public class CartService {

    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    GmallSmsClient gmallSmsClient;
    @Autowired
    GmallWmsClient gmallWmsClient;
    @Autowired
    LoginInterceptor loginInterceptor;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CartAsyncService cartAsyncService;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void addCart(Cart cart) {
        //判断是已登录&未登陆 设置到外层Key
        String userId = getUserId();
        //查询数据库redis是否有该数据 有就修改数量 无就新增记录(新增记录要先查skuId对应的信息)
        String skuId = cart.getSkuId().toString();
        BigDecimal newCount = cart.getCount();
        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(skuId)) {
            //有就修改数量
            cart = (Cart) hashOps.get(skuId);
            cart.setCount(cart.getCount().add(newCount));

            cartAsyncService.updateCart(userId, cart, skuId);
//            cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
        } else {
            //没有就新增到redis
            //默认属性
            cart.setCheck(true);
            cart.setUserId(userId);
            //pms
            SkuEntity skuEntity = gmallPmsClient.querySkuById(Long.parseLong(skuId)).getData();
            if (skuEntity != null) {
                cart.setPrice(skuEntity.getPrice());
                cart.setCurrentPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
                cart.setTitle(skuEntity.getTitle());
            }
            Map<Long, String> saleAttrMap = gmallPmsClient.querySaleAttrBySkuId(Long.parseLong(skuId)).getData();
            if (!CollectionUtils.isEmpty(saleAttrMap))
                cart.setSaleAttrs(saleAttrMap.get(skuId));
            //sms
            List<ItemSalesVo> sales = gmallSmsClient.allSales(Long.parseLong(skuId)).getData();
            if (!CollectionUtils.isEmpty(sales))
                cart.setSales(JSON.toJSONString(sales));
            //wms
            List<WareSkuEntity> wareSkuEntities = gmallWmsClient.queryWareSkuBySid(Long.parseLong(skuId)).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities))
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
                    return wareSkuEntity.getStock() > wareSkuEntity.getStockLocked();
                }));

            cartAsyncService.insertCart(userId, cart);
//            cartMapper.insert(cart);
        }
        hashOps.put(skuId, cart);
        redisTemplate.opsForValue().set(PRICE_PREFIX + cart.getSkuId(), cart.getCurrentPrice());
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = null;
        if (StringUtils.isBlank(userInfo.getUserId())) {
            userId = userInfo.getUserKey();
        } else {
            userId = userInfo.getUserId();
        }
        return userId;
    }

    public Cart queryCart(Long skuId) {
        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + getUserId());
        return (Cart) hashOps.get(skuId.toString());
    }

    public List queryCarts() {
        //1.如果是未登录状态 直接查询未登录的购物车并返回
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserId();
        String userKey = userInfo.getUserKey();

        BoundHashOperations unLoginOps = redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        List<Object> unLoginCarts = unLoginOps.values();
        if (StringUtils.isBlank(userId)) {
            if (CollectionUtils.isEmpty(unLoginCarts)) {
                return null;
            }
            return unLoginCarts.stream().map(o -> getCurrentPrice((Cart) o)).collect(Collectors.toList());
        }
        //2.是已登录状态 先查未登录的购物车是否有货
        BoundHashOperations loginOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> loginCarts = loginOps.values();
        //2.1.无货直接返回已登录的购物车 要返回查询最新价格
        if (CollectionUtils.isEmpty(unLoginCarts)) {
            if (CollectionUtils.isEmpty(loginCarts))
                return null;
            return loginCarts.stream().map(o -> getCurrentPrice((Cart) o)).collect(Collectors.toList());
        }

        //2.2.有货 要把未登录购物车添加到已登录购物车 并删除未登录购物车
        unLoginCarts.forEach(object -> {
            String json = JSON.toJSONString(object);
            Cart cart = (Cart) object;//JSON.parseObject(json, Cart.class);
            //要判断是否有当前skuId的货物,有就修改 没有才新增
            BigDecimal newCount = cart.getCount();
            if (loginOps.hasKey(cart.getSkuId().toString())) {
                //有就修改数量
                cart = (Cart) loginOps.get(cart.getSkuId().toString());
                cart.setCount(cart.getCount().add(newCount));
                cartAsyncService.updateCart(userId, cart, cart.getSkuId().toString());
            } else {
                cart.setUserId(userId);
                cartAsyncService.insertCart(userId, cart);
            }
            loginOps.put(cart.getSkuId().toString(), cart);

        });

        redisTemplate.delete(KEY_PREFIX + userKey);
        cartAsyncService.deleteCartsByUserId(userKey);
        //2.3再查redis中的已登录购物车
        List<Object> allLoginCarts = loginOps.values();
        if (CollectionUtils.isEmpty(allLoginCarts))
            return null;
        return allLoginCarts.stream().map(o -> getCurrentPrice((Cart) o)).collect(Collectors.toList());
    }

    private Cart getCurrentPrice(Cart o) {
        Cart cart = o;
        cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId()).toString()));
        return cart;
    }

    public void updateNum(Cart cart) {
        String userId = getUserId();
        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        Long skuId = cart.getSkuId();
        BigDecimal count = cart.getCount();
        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("购物车无此商品!");
        }
        cart = (Cart) hashOps.get(skuId.toString());
        cart.setCount(count);
        hashOps.put(skuId.toString(), cart);
        cartAsyncService.updateCart(userId, cart, skuId.toString());
    }

    public void updateStatus(Cart cart) {
        String userId = getUserId();
        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        Long skuId = cart.getSkuId();
        Boolean check = cart.getCheck();
        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("购物车无此商品!");
        }
        cart = (Cart) hashOps.get(skuId.toString());
        cart.setCheck(check);
        hashOps.put(skuId.toString(), cart);
        cartAsyncService.updateCart(userId, cart, skuId.toString());
    }

    public void deleteCart(Long skuId) {
        String userId = getUserId();
        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("购物车无此商品!");
        }
        hashOps.delete(skuId.toString());
        cartAsyncService.deleteCartBySkuId(userId, skuId);
    }

    public List<Cart> queryCheckedCartByUserId(String userId) {

        BoundHashOperations hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> values = hashOps.values();
        List<Cart> carts = values.stream().map(o -> (Cart) o).filter(Cart::getCheck).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(carts)) {
            throw new OrderException("没有选中购物车商品异常!");
        }
        return carts;
    }
}

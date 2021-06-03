package edu.sysu.gmall.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import edu.sysu.gmall.cart.mapper.CartMapper;
import edu.sysu.gmall.cart.pojo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-03 10:36
 */
@Service
public class CartAsyncService {
    @Autowired
    CartMapper cartMapper;

    @Async
    public void updateCart(Cart cart, String userId, String skuId) {
        cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }

    @Async
    public void insertCart(Cart cart) {
        cartMapper.insert(cart);
    }
    @Async
    public void deleteCartsByUserId(String userId){
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));
    }
    @Async
    public void deleteCartBySkuId(String userId,Long skuId){
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}

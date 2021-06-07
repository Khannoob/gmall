package edu.sysu.gmall.cart.controller;

import edu.sysu.gmall.common.exception.CartException;
import edu.sysu.gmall.cart.pojo.Cart;
import edu.sysu.gmall.cart.service.CartService;
import edu.sysu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-02 15:14
 */
@Controller
public class CartController {
    @Autowired
    CartService cartService;

    @GetMapping("query/check/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart> > queryCheckedCartByUserId(@PathVariable String userId) {
        List<Cart> carts = cartService.queryCheckedCartByUserId(userId);

        return ResponseVo.ok(carts);
    }

    @GetMapping
    public String toAddCart(Cart cart) {
        cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }

    @GetMapping("addCart.html")
    public String addCart(Long skuId, BigDecimal count, Model model) {
        Cart cart = cartService.queryCart(skuId);
        if (cart == null) {
            throw new CartException("购物车商品未找到!");
        }
        cart.setCount(count);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String cart(Model model) {
        List carts = cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart) {
        cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("updateStatus")
    @ResponseBody
    public ResponseVo updateStatus(@RequestBody Cart cart) {
        cartService.updateStatus(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(Long skuId) {
        cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

}

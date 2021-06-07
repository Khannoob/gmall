package edu.sysu.gmall.cart.api;

import edu.sysu.gmall.cart.pojo.Cart;
import edu.sysu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-06 17:02
 */

public interface GmallCartApi {
    @GetMapping("query/check/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartByUserId(@PathVariable String userId);
}

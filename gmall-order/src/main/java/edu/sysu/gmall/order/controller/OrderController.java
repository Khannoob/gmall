package edu.sysu.gmall.order.controller;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.oms.vo.OrderSubmitVo;
import edu.sysu.gmall.order.service.OrderService;
import edu.sysu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-05 16:40
 */
@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("confirm")
    public String confirmOrder(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirmVo", confirmVo);
        return "trade";
    }


    @PostMapping("submit")
    @ResponseBody
    public ResponseVo submitOrder(@RequestBody OrderSubmitVo orderSubmitVo) {
        orderService.submitOrder(orderSubmitVo);

        return ResponseVo.ok(orderSubmitVo.getOrderToken());
    }
}

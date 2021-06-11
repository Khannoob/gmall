package edu.sysu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import edu.sysu.gmall.common.exception.OrderException;
import edu.sysu.gmall.common.exception.PaymentException;
import edu.sysu.gmall.oms.entity.OrderEntity;
import edu.sysu.gmall.payment.config.AlipayTemplate;
import edu.sysu.gmall.payment.entity.PaymentInfoEntity;
import edu.sysu.gmall.payment.vo.PayAsyncVo;
import edu.sysu.gmall.payment.vo.PayVo;
import edu.sysu.gmall.payment.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-08 08:59
 */
@Controller
public class PaymentController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    PaymentService paymentService;
    @Autowired
    RabbitTemplate rabbitTemplate;


    @GetMapping("pay.html")
    public String toPay(@RequestParam String orderToken, Model model) {
        System.out.println("orderToken = " + orderToken);
        //查询order的信息 返回给页面
        OrderEntity orderEntity = paymentService.queryOrder(orderToken);

        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }

    /**
     * 生成订单二维码页面 供用户支付
     *
     * @return 返回的是二维码表单页面
     */
    @GetMapping("alipay.html")
    @ResponseBody
    public String alipay(String orderToken) {
        try {
            System.out.println("orderToken = " + orderToken);
            OrderEntity orderEntity = paymentService.queryOrder(orderToken);
            System.out.println("PAYSERVICE+orderEntity = " + orderEntity);
            if (orderEntity == null) {
                throw new PaymentException("服务器错误!");
            }
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setSubject("请退款到上海1130JAVAKhan!");
            //0.01
            payVo.setTotal_amount("0.01");
            //先把对账信息保存到对账表
            Long payId = paymentService.savePayment(orderEntity, 1);
            payVo.setPassback_params(payId.toString());
            String form = alipayTemplate.pay(payVo);
            return form;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new PaymentException("支付出错，请刷新后重试！");
        }
    }

    /**
     * 同步回调接口 直接返回支付成功页面
     *
     * @param payAsyncVo
     * @return
     */
    @GetMapping("pay/ok")
    public String payOk(PayAsyncVo payAsyncVo) {
//        System.out.println("payAsyncVo = " + payAsyncVo);
//        System.out.println(11111111);
        // 查询订单数据展示在支付成功页面
        // String orderToken = payAsyncVo.getOut_trade_no();
        // TODO：查询并通过model响应给页面
        return "paysuccess";
    }

    /**
     * 异步回调接口更可靠 完成订单修改 库存减少 销量增加
     *
     * @param payAsyncVo
     * @return
     */
    @PostMapping("pay/success")
    @ResponseBody
    public String paySuccess(PayAsyncVo payAsyncVo) {
        System.out.println(1);
        Boolean aBoolean = alipayTemplate.checkSignature(payAsyncVo);
        //1.延签
        if (!aBoolean) {
            //保存日志记录失败
            return "failure";
        }
        System.out.println(2);
        //2.检查业务参数
        String app_id = payAsyncVo.getApp_id();
        String appId = alipayTemplate.getApp_id();

        String payId = payAsyncVo.getPassback_params();
        PaymentInfoEntity paymentInfoEntity = paymentService.queryPaymentInfoEntity(payId);
        String out_trade_no = payAsyncVo.getOut_trade_no();
        String outTradeNo = paymentInfoEntity.getOutTradeNo();

        String total_amount = payAsyncVo.getTotal_amount();
        BigDecimal totalAmount = paymentInfoEntity.getTotalAmount();
        if (StringUtils.equals(app_id, appId) && StringUtils.equals(out_trade_no, outTradeNo)
                && new BigDecimal(total_amount).compareTo(totalAmount) == 0) {
            System.out.println(3);
            //3.校验支付状态
            if (!StringUtils.equals("TRADE_SUCCESS", payAsyncVo.getTrade_status())) {
                return "failure";
            }
            System.out.println(4);
            //4.支付成功 更改记账本
            paymentService.updatePayment(payAsyncVo);
            //5.MQ修改订单状态并减库存
            System.out.println(5);
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.success", payAsyncVo.getOut_trade_no());
            System.out.println(payAsyncVo.getOut_trade_no());
            //6.返回success给支付宝
            return "success";

        }
        return "failure";
    }

}

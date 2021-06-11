package edu.sysu.gmall.payment.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.sysu.gmall.oms.entity.OrderEntity;
import edu.sysu.gmall.payment.GmallOmsClient;
import edu.sysu.gmall.payment.entity.PaymentInfoEntity;
import edu.sysu.gmall.payment.mapper.PaymentMapper;
import edu.sysu.gmall.payment.vo.PayAsyncVo;
import edu.sysu.gmall.payment.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-08 11:33
 */
@Service
public class PaymentService {
    @Autowired
    GmallOmsClient omsClient;

    @Autowired
    PaymentMapper paymentMapper;

    public OrderEntity queryOrder(String orderToken) {
        System.out.println("orderToken = " + orderToken);
        return omsClient.queryOrderByOrderSn(orderToken).getData();
    }


    public Long savePayment(OrderEntity orderEntity, Integer payType) {
        // 查看支付记录，是否已存在。
//        PaymentInfoEntity paymentInfoEntity = this.paymentMapper.selectOne(new QueryWrapper<PaymentInfoEntity>().eq("out_trade_no", orderEntity.getOrderSn()));
//        // 如果存在，直接结束
//        if (paymentInfoEntity != null) {
//            return paymentInfoEntity.getId();
//        }
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setCreateTime(new Date());
        paymentInfoEntity.setPaymentType(payType);
        paymentInfoEntity.setPaymentStatus(0);
        paymentInfoEntity.setOutTradeNo(orderEntity.getOrderSn());
        paymentInfoEntity.setSubject("请退款到上海1130JAVAKhan");
        //得写成0.01
        paymentInfoEntity.setTotalAmount(orderEntity.getTotalAmount());
        paymentMapper.insert(paymentInfoEntity);
        return paymentInfoEntity.getId();
    }

    public PaymentInfoEntity queryPaymentInfoEntity(String payId) {
        return paymentMapper.selectById(payId);
    }

    public void updatePayment(PayAsyncVo payAsyncVo) {
        PaymentInfoEntity paymentInfoEntity = paymentMapper.selectById(payAsyncVo.getPassback_params());
        paymentInfoEntity.setTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        paymentMapper.updateById(paymentInfoEntity);
    }
}

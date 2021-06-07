package edu.sysu.gmall.order.vo;

import edu.sysu.gmall.oms.vo.OrderItemVo;
import edu.sysu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-05 13:01
 */
@Data
public class OrderConfirmVo {
    private List<OrderItemVo> items;//送货清单
    private List<UserAddressEntity> addresses;//地址列表
    private Integer bounds;//积分
    private String orderToken;//防重标识

}

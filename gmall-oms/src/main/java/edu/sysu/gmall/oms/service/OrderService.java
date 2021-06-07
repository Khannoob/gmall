package edu.sysu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.oms.entity.OrderEntity;
import edu.sysu.gmall.oms.vo.OrderSubmitVo;

import java.util.Map;

/**
 * 订单
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-06-05 15:25:30
 */
public interface OrderService extends IService<OrderEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    OrderEntity saveOrder(OrderSubmitVo orderSubmitVO, Long userId);
}


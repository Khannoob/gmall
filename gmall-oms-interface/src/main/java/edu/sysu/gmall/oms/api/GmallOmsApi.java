package edu.sysu.gmall.oms.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.oms.entity.OrderEntity;
import edu.sysu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-07 14:01
 */
public interface GmallOmsApi {
    @PostMapping("oms/order/{userId}")
    public ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSubmitVo orderSubmitVO, @PathVariable("userId")Long userId);
}

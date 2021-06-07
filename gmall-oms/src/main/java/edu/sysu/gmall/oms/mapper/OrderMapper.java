package edu.sysu.gmall.oms.mapper;

import edu.sysu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-06-05 15:25:30
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}

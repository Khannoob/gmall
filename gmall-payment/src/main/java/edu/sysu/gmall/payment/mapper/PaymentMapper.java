package edu.sysu.gmall.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.sysu.gmall.payment.entity.PaymentInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-08 20:23
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentInfoEntity> {

}

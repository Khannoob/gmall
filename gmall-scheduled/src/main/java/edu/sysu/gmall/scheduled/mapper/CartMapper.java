package edu.sysu.gmall.scheduled.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.sysu.gmall.cart.pojo.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-04 20:47
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    
}

package edu.sysu.gmall.ums.mapper;

import edu.sysu.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址表
 * 
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 19:13:43
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
	
}

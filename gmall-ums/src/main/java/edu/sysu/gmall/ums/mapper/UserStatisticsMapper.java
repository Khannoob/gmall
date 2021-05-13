package edu.sysu.gmall.ums.mapper;

import edu.sysu.gmall.ums.entity.UserStatisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统计信息表
 * 
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 19:13:42
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatisticsEntity> {
	
}

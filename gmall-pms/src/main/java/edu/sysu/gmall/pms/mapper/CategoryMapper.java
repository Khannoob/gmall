package edu.sysu.gmall.pms.mapper;

import edu.sysu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
	
}

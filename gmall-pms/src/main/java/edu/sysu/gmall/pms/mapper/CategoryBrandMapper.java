package edu.sysu.gmall.pms.mapper;

import edu.sysu.gmall.pms.entity.CategoryBrandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌分类关联
 * 
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
@Mapper
public interface CategoryBrandMapper extends BaseMapper<CategoryBrandEntity> {
	
}

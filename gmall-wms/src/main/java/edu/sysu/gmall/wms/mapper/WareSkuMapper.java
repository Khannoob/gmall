package edu.sysu.gmall.wms.mapper;

import edu.sysu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-14 19:41:47
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkWare(@Param("count") Integer count, @Param("skuId") Long skuId);

    int lockWare(@Param("id") Long id,@Param("count") Integer count);

    int unlockWare(@Param("id") Long id, @Param("count") Integer count);
}

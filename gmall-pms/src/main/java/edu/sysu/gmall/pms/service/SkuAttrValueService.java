package edu.sysu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.pms.vo.SaleAttrValueVo;
import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySkuAttrValueEntityBySkuIdAndCid(Long skuId,Long cid);

    List<SaleAttrValueVo> querySaleAttrsBySpuId(Long spuId);

    Map<Long, String> querySaleAttrBySkuId(Long skuId);

    String queryMappingBySpuId(Long spuId);
}


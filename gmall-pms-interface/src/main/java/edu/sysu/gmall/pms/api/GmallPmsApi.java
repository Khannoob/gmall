package edu.sysu.gmall.pms.api;

import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-19 18:37
 */
public interface GmallPmsApi {

    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @PostMapping("pms/spu/page")
    public ResponseVo<List<SpuEntity>> querySpuByPageSearch(PageParamVo paramVo);

    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpu(@PathVariable Long spuId);

    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryL2CategoriesByPid(@PathVariable Long pid);

    @GetMapping("pms/category/parent/{ParentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesByPid(@PathVariable Long ParentId);

    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> queryAttrValueBySpuIdAndCid(@PathVariable Long spuId, @RequestParam Long cid);

    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueEntityBySkuIdAndCid(@PathVariable Long skuId, @RequestParam Long cid);
}

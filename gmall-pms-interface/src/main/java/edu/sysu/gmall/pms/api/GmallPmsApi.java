package edu.sysu.gmall.pms.api;

import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.entity.*;
import edu.sysu.gmall.pms.vo.ItemGroupVo;
import edu.sysu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

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

    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> queryCatesL123ByL3Cid(@PathVariable Long cid);

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

    @GetMapping("pms/skuattrvalue/all/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrsBySpuId(@PathVariable Long spuId);

    @GetMapping("pms/skuattrvalue/item/sku/{skuId}")
    public ResponseVo<Map<Long, String>> querySaleAttrBySkuId(@PathVariable Long skuId);

    @GetMapping("/pms/skuattrvalue/mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable Long spuId);

    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> querySkuImagesBySKuId(@PathVariable Long skuId);

    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupAttrsByCidSpuIdSkuId(
            @PathVariable Long cid,
            @RequestParam Long spuId,
            @RequestParam Long skuId);
}

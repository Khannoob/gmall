package edu.sysu.gmall.pms.controller;

import java.util.List;
import java.util.Map;

import edu.sysu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;
import edu.sysu.gmall.pms.service.SkuAttrValueService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.common.bean.PageParamVo;

/**
 * sku销售属性&值
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @GetMapping("mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable Long spuId){
        String mapping = skuAttrValueService.queryMappingBySpuId(spuId);
        return ResponseVo.ok(mapping);
    }

    @GetMapping("item/sku/{skuId}")
    public ResponseVo<Map<Long,String>> querySaleAttrBySkuId(@PathVariable Long skuId){
        Map<Long,String> saleAttr = skuAttrValueService.querySaleAttrBySkuId(skuId);
        return ResponseVo.ok(saleAttr);
    }

    @GetMapping("all/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrsBySpuId(@PathVariable Long spuId){
        List<SaleAttrValueVo> saleAttrValueVos = skuAttrValueService.querySaleAttrsBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);
    }

    @GetMapping("sku/list/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueEntityListBySkuId(@PathVariable Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueService.querySkuAttrValueEntityListBySkuId(skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    @GetMapping("sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueEntityBySkuIdAndCid(
            @PathVariable Long skuId,
            @RequestParam Long cid) {
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueService.querySkuAttrValueEntityBySkuIdAndCid(skuId, cid);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id) {
        SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}

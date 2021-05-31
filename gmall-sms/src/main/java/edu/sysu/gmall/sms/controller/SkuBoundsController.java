package edu.sysu.gmall.sms.controller;

import java.util.List;

import edu.sysu.gmall.sms.vo.ItemSalesVo;
import edu.sysu.gmall.sms.vo.SkuSaleVo;
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

import edu.sysu.gmall.sms.entity.SkuBoundsEntity;
import edu.sysu.gmall.sms.service.SkuBoundsService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.common.bean.PageParamVo;

/**
 * 商品spu积分设置
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 18:18:40
 */
@Api(tags = "商品spu积分设置 管理")
@RestController
@RequestMapping("sms/skubounds")
public class SkuBoundsController {

    @Autowired
    private SkuBoundsService skuBoundsService;

    @GetMapping("all/{skuId}")
    public ResponseVo<List<ItemSalesVo>> allSales(@PathVariable Long skuId){
        List<ItemSalesVo> itemSalesVos = skuBoundsService.allSales(skuId);
        return ResponseVo.ok(itemSalesVos);
    }

    @ApiOperation("大保存")
    @PostMapping("sales/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo){
        skuBoundsService.bigSave(skuSaleVo);
        return ResponseVo.ok();
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuBoundsByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuBoundsService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuBoundsEntity> querySkuBoundsById(@PathVariable("id") Long id){
		SkuBoundsEntity skuBounds = skuBoundsService.getById(id);

        return ResponseVo.ok(skuBounds);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuBoundsEntity skuBounds){
		skuBoundsService.save(skuBounds);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuBoundsEntity skuBounds){
		skuBoundsService.updateById(skuBounds);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuBoundsService.removeByIds(ids);

        return ResponseVo.ok();
    }

}

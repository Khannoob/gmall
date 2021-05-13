package edu.sysu.gmall.sms.controller;

import java.util.List;

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

import edu.sysu.gmall.sms.entity.CouponSpuCategoryEntity;
import edu.sysu.gmall.sms.service.CouponSpuCategoryService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.common.bean.PageParamVo;

/**
 * 优惠券分类关联
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 18:18:40
 */
@Api(tags = "优惠券分类关联 管理")
@RestController
@RequestMapping("sms/couponspucategory")
public class CouponSpuCategoryController {

    @Autowired
    private CouponSpuCategoryService couponSpuCategoryService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCouponSpuCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = couponSpuCategoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CouponSpuCategoryEntity> queryCouponSpuCategoryById(@PathVariable("id") Long id){
		CouponSpuCategoryEntity couponSpuCategory = couponSpuCategoryService.getById(id);

        return ResponseVo.ok(couponSpuCategory);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CouponSpuCategoryEntity couponSpuCategory){
		couponSpuCategoryService.save(couponSpuCategory);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CouponSpuCategoryEntity couponSpuCategory){
		couponSpuCategoryService.updateById(couponSpuCategory);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		couponSpuCategoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}

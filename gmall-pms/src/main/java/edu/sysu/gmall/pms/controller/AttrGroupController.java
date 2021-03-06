package edu.sysu.gmall.pms.controller;

import java.util.List;

import edu.sysu.gmall.pms.vo.ItemGroupVo;
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

import edu.sysu.gmall.pms.entity.AttrGroupEntity;
import edu.sysu.gmall.pms.service.AttrGroupService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupAttrsByCidSpuIdSkuId(
            @PathVariable Long cid,
            @RequestParam Long spuId,
            @RequestParam Long skuId){
        List<ItemGroupVo> itemGroupVos = attrGroupService.queryGroupAttrsByCidSpuIdSkuId(cid,spuId,skuId);
        return ResponseVo.ok(itemGroupVos);
    }
    @GetMapping("withattrs/{catId}")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupAndAttrByCid(@PathVariable String catId){
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.queryAttrGroupAndAttrByCid(catId);
        return ResponseVo.ok(attrGroupEntities);
    }


    @GetMapping("category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupByCid(@PathVariable Long cid){
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.queryAttrGroupByCid(cid);
        return ResponseVo.ok(attrGroupEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}

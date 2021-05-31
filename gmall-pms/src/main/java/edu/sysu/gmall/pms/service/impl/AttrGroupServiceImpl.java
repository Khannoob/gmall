package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.entity.AttrEntity;
import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;
import edu.sysu.gmall.pms.entity.SpuAttrValueEntity;
import edu.sysu.gmall.pms.mapper.AttrMapper;
import edu.sysu.gmall.pms.mapper.SkuAttrValueMapper;
import edu.sysu.gmall.pms.mapper.SpuAttrValueMapper;
import edu.sysu.gmall.pms.service.AttrGroupService;
import edu.sysu.gmall.pms.service.AttrService;
import edu.sysu.gmall.pms.vo.AttrValueVo;
import edu.sysu.gmall.pms.vo.ItemGroupVo;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.AttrGroupMapper;
import edu.sysu.gmall.pms.entity.AttrGroupEntity;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public List<AttrGroupEntity> queryAttrGroupByCid(Long cid) {
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrGroupEntity::getCategoryId, cid);
        return this.list(queryWrapper);
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupAndAttrByCid(String catId) {
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
        List<AttrGroupEntity> list = this.list(queryWrapper.eq(AttrGroupEntity::getCategoryId, catId));
        if (CollectionUtils.isEmpty(list))
            return null;
        list.forEach(t -> {
            LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AttrEntity::getGroupId, t.getId()).eq(AttrEntity::getType, 1);
            t.setAttrEntities(attrService.list(wrapper));
        });
        return list;
    }

    @Autowired
    AttrMapper attrMapper;
    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public List<ItemGroupVo> queryGroupAttrsByCidSpuIdSkuId(Long cid, Long spuId, Long skuId) {

        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }
        return attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupName(attrGroupEntity.getName());
            //根据group_id查询所有attrEntity
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));

            if (CollectionUtils.isEmpty(attrEntities)) {
                return itemGroupVo;
            }
            //获取attrId集合
            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
            ArrayList<AttrValueVo> attrValueVos = new ArrayList<>();

            //在spu_attr_value表里查 spuId和attrId
            List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().
                    eq("spu_id", spuId).in("attr_id", attrIds));
            if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList()));
            }

            //在sku_attr_value表里查 参数 skuId和attrId
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().
                    eq("sku_id", spuId).in("attr_id", attrIds));
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                attrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList()));
            }


            itemGroupVo.setAttrValues(attrValueVos);
            return itemGroupVo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

}
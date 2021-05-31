package edu.sysu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.entity.*;
import edu.sysu.gmall.pms.mapper.AttrMapper;
import edu.sysu.gmall.pms.mapper.SkuMapper;
import edu.sysu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.SkuAttrValueMapper;
import edu.sysu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    AttrMapper attrMapper;

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueEntityBySkuIdAndCid(Long skuId, Long cid) {
        List<AttrEntity> attrEntities = attrMapper.selectList(new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getCategoryId, cid)
                .eq(AttrEntity::getSearchType, 1));
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attrIds)) {
            return null;
        }
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new LambdaQueryWrapper<SkuAttrValueEntity>().
                eq(SkuAttrValueEntity::getSkuId, skuId).in(SkuAttrValueEntity::getAttrId, attrIds));
        return skuAttrValueEntities;
    }

    @Autowired
    SkuMapper skuMapper;

    @Override
    public List<SaleAttrValueVo> querySaleAttrsBySpuId(Long spuId) {
        //通过map转化成一个SaleAttrValueVo对象
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (!CollectionUtils.isEmpty(skuEntities)) {
            List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
            List<SkuAttrValueEntity> skuAttrValueEntities = baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds)
                    .orderByAsc("attr_id"));
            if (skuAttrValueEntities != null) {
                Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
                map.forEach((attrId, skuAttrEntities) -> {
                    SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
                    saleAttrValueVo.setAttrId(attrId);
                    saleAttrValueVo.setAttrName(skuAttrEntities.get(0).getAttrName());
                    Set<String> attrValues = skuAttrEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
                    saleAttrValueVo.setAttrValues(attrValues);
                    saleAttrValueVos.add(saleAttrValueVo);
                });
            }
        }
        return saleAttrValueVos;
    }

    @Override
    public Map<Long, String> querySaleAttrBySkuId(Long skuId) {
        Map<Long, String> saleAttrMap = new HashMap<>();
        List<SkuAttrValueEntity> skuAttrValueEntities = baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
            saleAttrMap = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
        }
        return saleAttrMap;
    }

    @Override
    public String queryMappingBySpuId(Long spuId) {
        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        List<Map<String,Object>> maps = baseMapper.queryMappingBySpuId(skuIds);
        if (CollectionUtils.isEmpty(maps)){
            return null;
        }
        Map<String, Long> mapping = maps.stream().collect(Collectors.toMap(map -> map.get("attrValues").toString(), map -> (long)map.get("sku_id")));
        return JSON.toJSONString(mapping);
    }
}
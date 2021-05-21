package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.entity.AttrEntity;
import edu.sysu.gmall.pms.entity.SpuAttrValueEntity;
import edu.sysu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.SkuAttrValueMapper;
import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;
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
    public List<SkuAttrValueEntity> querySkuAttrValueEntityBySkuIdAndCid(Long skuId,Long cid) {
        List<AttrEntity> attrEntities = attrMapper.selectList(new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getCategoryId, cid)
                .eq(AttrEntity::getSearchType, 1));
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attrIds)){
            return null;
        }
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new LambdaQueryWrapper<SkuAttrValueEntity>().
                eq(SkuAttrValueEntity::getSkuId, skuId).in(SkuAttrValueEntity::getAttrId, attrIds));
        return skuAttrValueEntities;
    }

}
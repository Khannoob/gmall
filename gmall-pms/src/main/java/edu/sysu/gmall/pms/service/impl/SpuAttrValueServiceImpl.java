package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.entity.AttrEntity;
import edu.sysu.gmall.pms.mapper.AttrMapper;
import edu.sysu.gmall.pms.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.SpuAttrValueMapper;
import edu.sysu.gmall.pms.entity.SpuAttrValueEntity;
import edu.sysu.gmall.pms.service.SpuAttrValueService;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    AttrMapper attrMapper;
    @Override
    public List<SpuAttrValueEntity> queryAttrValueBySpuIdAndCid(Long spuId, Long cid) {
        List<AttrEntity> attrEntities = attrMapper.selectList(new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getCategoryId, cid)
                .eq(AttrEntity::getSearchType, 1));
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attrIds)){
            return null;
        }
        List<SpuAttrValueEntity> spuAttrValueEntities = this.list(new LambdaQueryWrapper<SpuAttrValueEntity>().
                eq(SpuAttrValueEntity::getSpuId, spuId).in(SpuAttrValueEntity::getAttrId, attrIds));
        return spuAttrValueEntities;
    }

}
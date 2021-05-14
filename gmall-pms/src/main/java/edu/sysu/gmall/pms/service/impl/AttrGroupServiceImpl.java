package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.entity.AttrEntity;
import edu.sysu.gmall.pms.mapper.AttrMapper;
import edu.sysu.gmall.pms.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.AttrGroupMapper;
import edu.sysu.gmall.pms.entity.AttrGroupEntity;
import edu.sysu.gmall.pms.service.AttrGroupService;
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
            wrapper.eq(AttrEntity::getGroupId, t.getId()).eq(AttrEntity::getType,1);
            t.setAttrEntities(attrService.list(wrapper));
        });
        return list;
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
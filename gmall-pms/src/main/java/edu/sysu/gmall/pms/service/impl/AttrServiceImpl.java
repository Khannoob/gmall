package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.AttrMapper;
import edu.sysu.gmall.pms.entity.AttrEntity;
import edu.sysu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrEntity> queryAttrByGid(Long gid) {
        return baseMapper.selectList(new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getGroupId,gid));
    }

    @Override
    public List<AttrEntity> querySkuAttrByCidOrTypeOrSearchType(Long cid, Integer type, Integer searchType) {
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrEntity::getCategoryId,cid);
        if (type!=null){
            queryWrapper.eq(AttrEntity::getType,type);
        }
        if (searchType != null) {
            queryWrapper.eq(AttrEntity::getSearchType,searchType);
        }
        return this.list(queryWrapper);
    }


}
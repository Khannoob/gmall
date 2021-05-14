package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.SpuMapper;
import edu.sysu.gmall.pms.entity.SpuEntity;
import edu.sysu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo queryPageByCid(Long categoryId, PageParamVo paramVo) {
        LambdaQueryWrapper<SpuEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (0!=categoryId)
        queryWrapper.eq(SpuEntity::getCategoryId,categoryId);

        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key))
            queryWrapper.and(t -> t.eq(SpuEntity::getId,key).or().like(SpuEntity::getName,key));

        IPage<SpuEntity> page = this.page(paramVo.getPage(), queryWrapper);

        return new PageResultVo(page);
    }


}
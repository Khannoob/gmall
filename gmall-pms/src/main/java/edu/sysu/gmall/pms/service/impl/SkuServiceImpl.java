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

import edu.sysu.gmall.pms.mapper.SkuMapper;
import edu.sysu.gmall.pms.entity.SkuEntity;
import edu.sysu.gmall.pms.service.SkuService;


@Service("skuService")
public class SkuServiceImpl extends ServiceImpl<SkuMapper, SkuEntity> implements SkuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuEntity> querySkusBySpu(String spuId) {
        return this.list(new LambdaQueryWrapper<SkuEntity>().eq(SkuEntity::getSpuId,spuId));
    }

}
package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.mapper.SpuDescMapper;
import edu.sysu.gmall.pms.vo.SpuVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    @Autowired
    private SpuDescMapper spuDescMapper;
    @Override
    public void bigSave(SpuVo spuVo) {
        //1.保存spu相关信息
        //1.1 保存pms_spu表
        SpuEntity spuEntity = new SpuEntity();
        BeanUtils.copyProperties(spuVo,spuEntity);

        Date date = new Date();
        spuEntity.setCreateTime(date);
        spuEntity.setUpdateTime(date);
        this.save(spuEntity);
        Long spuId = spuEntity.getId();
        //1.2 保存pms_spu_desc表

        //1.3 保存pms_spu_attr_value表

        //2.保存sku相关信息
        //2.1 保存pms_sku表
        //2.2 保存pms_sku_images表
        //2.3 保存pms_sku_attr_value表

        //3.保存sms_sku相关信息
        //3.1 保存sms_sku_bounds表
        //3.2 保存sms_sku_full_reduction表
        //3.3 保存sms_sku_ladder表
    }


}
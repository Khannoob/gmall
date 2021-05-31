package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.entity.*;
import edu.sysu.gmall.pms.feign.GmallSmsClient;
import edu.sysu.gmall.pms.mapper.SpuDescMapper;
import edu.sysu.gmall.pms.service.*;
import edu.sysu.gmall.pms.vo.SkuVo;
import edu.sysu.gmall.pms.vo.SpuAttrVo;
import edu.sysu.gmall.pms.vo.SpuVo;
import edu.sysu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


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
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;
    @Autowired
    SkuService skuService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient smsFeignClientApi;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @GlobalTransactional(rollbackFor = Exception.class)
//    @Transactional(rollbackFor = Exception.class)
    @Override
    public void bigSave(SpuVo spuVo) {
        long start = System.currentTimeMillis();
        //1.保存spu相关信息
        //1.1 保存pms_spu表
        Long spuId = this.saveSpu(spuVo);

        //1.2 保存pms_spu_desc表
        saveSpuDesc(spuVo, spuId);

        //1.3 保存pms_spu_attr_value表
        saveSpuAttrEntity(spuVo, spuId);

        //2.保存sku相关信息
        saveSKuVo(spuVo, spuId);


        //通过RabbitMQ异步保存到ES数据库中
        rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.insert",spuId);
        System.out.println("花费时间:"+(System.currentTimeMillis()-start));
    }

    private void saveSKuVo(SpuVo spuVo, Long spuId) {
        List<SkuVo> skus = spuVo.getSkus();
        if (!CollectionUtils.isEmpty(skus)){
            skus.forEach(skuVo->{

                //2.1 保存pms_sku表
                skuVo.setSpuId(spuId);
                List<String> images = skuVo.getImages();
                if (!CollectionUtils.isEmpty(images)){
                    skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage())?skuVo.getDefaultImage():images.get(0));
                }
                skuVo.setCategoryId(spuVo.getCategoryId());
                skuVo.setBrandId(spuVo.getBrandId());
                skuService.save(skuVo);
                Long skuId = skuVo.getId();
                //2.2 保存pms_sku_images表
                if (!CollectionUtils.isEmpty(images)){
                    List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setUrl(image);
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setSort(0);
                        skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0);
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
                    skuImagesService.saveBatch(skuImagesEntities);
                }
                //2.3 保存pms_sku_attr_value表
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                if (!CollectionUtils.isEmpty(saleAttrs)){
                    saleAttrs.forEach(saleAttr->{
                        saleAttr.setSkuId(skuId);
                        saleAttr.setSort(0);
                    });
                    skuAttrValueService.saveBatch(saleAttrs);
                }
                //3.保存sms_sku相关信息
                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuVo,skuSaleVo);
                skuSaleVo.setSkuId(skuId);
                smsFeignClientApi.saveSales(skuSaleVo);
            });
        }

    }

    private void saveSpuAttrEntity(SpuVo spuVo, Long spuId) {
        List<SpuAttrVo> baseAttrs = spuVo.getBaseAttrs();
        List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(SpuAttrVo -> {
            SpuAttrVo.setSpuId(spuId);
            SpuAttrVo.setSort(0);
            return SpuAttrVo;
        }).collect(Collectors.toList());
        spuAttrValueService.saveBatch(spuAttrValueEntities);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSpuDesc(SpuVo spuVo, Long spuId) {
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        List<String> spuImages = spuVo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)){
            String join = StringUtils.join(spuImages, ",");
            spuDescEntity.setDecript(join);
        }
        spuDescMapper.insert(spuDescEntity);
    }


    private Long saveSpu(SpuVo spuVo) {
        SpuEntity spuEntity = new SpuEntity();
        BeanUtils.copyProperties(spuVo,spuEntity);

        Date date = new Date();
        spuEntity.setCreateTime(date);
        spuEntity.setUpdateTime(date);
        this.save(spuEntity);
        return spuEntity.getId();
    }


}
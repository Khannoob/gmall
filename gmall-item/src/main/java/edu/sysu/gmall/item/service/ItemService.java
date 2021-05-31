package edu.sysu.gmall.item.service;

import edu.sysu.gmall.item.feign.GmallPmsClient;
import edu.sysu.gmall.item.feign.GmallSmsClient;
import edu.sysu.gmall.item.feign.GmallWmsClient;
import edu.sysu.gmall.item.vo.ItemVo;
import edu.sysu.gmall.pms.entity.*;
import edu.sysu.gmall.pms.vo.ItemGroupVo;
import edu.sysu.gmall.pms.vo.SaleAttrValueVo;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-31 10:16
 */
@Service
public class ItemService {
    @Autowired
    GmallPmsClient gmallPmsApi;
    @Autowired
    GmallWmsClient gmallWmsApi;
    @Autowired
    GmallSmsClient gmallSmsApi;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    TemplateEngine templateEngine;

    public ItemVo itemDetail(Long skuId) {
        ItemVo itemVo = new ItemVo();
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            SkuEntity skuEntity = gmallPmsApi.querySkuById(skuId).getData();
            if (skuEntity == null)
                return null;
            else {
//        4.查Sku信息 V 1
                itemVo.setSkuId(skuId);
                itemVo.setTitle(skuEntity.getTitle());
                itemVo.setSubTitle(skuEntity.getSubtitle());
                itemVo.setDefaultImage(skuEntity.getDefaultImage());
                itemVo.setPrice(skuEntity.getPrice());
                itemVo.setWeight(skuEntity.getWeight());
                return skuEntity;
            }
        }, threadPoolExecutor);


//        1.查123级分类 V 2
        CompletableFuture<Void> categoryFuture = skuFuture.thenAcceptAsync(skuEntity -> {
                    Long categoryId = skuEntity.getCategoryId();
                    List<CategoryEntity> categoryEntities = gmallPmsApi.queryCatesL123ByL3Cid(categoryId).getData();
                    if (!CollectionUtils.isEmpty(categoryEntities))
                        itemVo.setCategoryEntities(categoryEntities);
                }, threadPoolExecutor);


//        2.查品牌 V 2
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
                    Long brandId = skuEntity.getBrandId();
                    BrandEntity brandEntity = gmallPmsApi.queryBrandById(brandId).getData();
                    if (brandEntity != null) {
                        itemVo.setBrandId(brandId);
                        itemVo.setBrandName(brandEntity.getName());
                    }
                }, threadPoolExecutor);

//        3.查Spu信息 V 2 拿到spuId才能查哈
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            Long spuId = skuEntity.getSpuId();
            SpuEntity spuEntity = gmallPmsApi.querySpuById(spuId).getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuId);
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);


//        5.查促销 V 1
        CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(() -> {
            List<ItemSalesVo> itemSalesVos = gmallSmsApi.allSales(skuId).getData();
            if (!CollectionUtils.isEmpty(itemSalesVos))
                itemVo.setSales(itemSalesVos);
        }, threadPoolExecutor);


//        6.是否有货 V 1
        CompletableFuture<Void> wmsFuture = CompletableFuture.runAsync(() -> {
            List<WareSkuEntity> wareSkuEntities = gmallWmsApi.queryWareSkuBySid(skuId).getData();
            if (CollectionUtils.isEmpty(wareSkuEntities))
                itemVo.setStore(false);
            else {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
                    return wareSkuEntity.getStock() > 0 && wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0;
                }));
            }
        }, threadPoolExecutor);

//        7.一个Spu下的所有Sku的销售属性 V 2
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<SaleAttrValueVo> saleAttrValueVos = gmallPmsApi.querySaleAttrsBySpuId(skuEntity.getSpuId()).getData();
            if (!CollectionUtils.isEmpty(saleAttrValueVos))
                itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);

//        8.sku所有的图片	images V 1
        CompletableFuture<Void> skuImagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = gmallPmsApi.querySkuImagesBySKuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuImagesEntities))
                itemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);

//        9.当前sku具有的销售属性 V 为了红框标注 1
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            Map<Long, String> saleAttr = gmallPmsApi.querySaleAttrBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(saleAttr))
                itemVo.setSaleAttr(saleAttr);
        }, threadPoolExecutor);


//        10.查映射关系 {白天白,8G,128: 10, 白天白,12G,256G: 20} attrValue : skuId 为了做页面跳转 V 2
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            String mapping = gmallPmsApi.queryMappingBySpuId(skuEntity.getSpuId()).getData();
            if (StringUtils.isNotBlank(mapping))
                itemVo.setMapping(mapping);
        }, threadPoolExecutor);


//        11.根据spuId查spu详细信息 V 2
        CompletableFuture<Void> spuDescFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            SpuDescEntity spuDescEntity = gmallPmsApi.querySpuDescById(skuEntity.getSpuId()).getData();
            if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {
                itemVo.setSpuDesc(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);

//        12.查询当前spu和sku所有分组属性 V 2
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<ItemGroupVo> itemGroupVos = gmallPmsApi.queryGroupAttrsByCidSpuIdSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId).getData();
            if (!CollectionUtils.isEmpty(itemGroupVos))
                itemVo.setGroups(itemGroupVos);
        }, threadPoolExecutor);

        CompletableFuture.allOf(categoryFuture,brandFuture,spuFuture,smsFuture,
                wmsFuture,saleAttrsFuture,skuImagesFuture,saleAttrFuture,mappingFuture,spuDescFuture,groupFuture).join();
        threadPoolExecutor.execute(()->{
            generateHtml(itemVo);
        });
        return itemVo;
    }

    public ItemVo itemDetail2(Long skuId) {
        ItemVo itemVo = new ItemVo();
        SkuEntity skuEntity = gmallPmsApi.querySkuById(skuId).getData();
        if (skuEntity == null)
            return null;

//        1.查123级分类 V 2
        Long categoryId = skuEntity.getCategoryId();
        List<CategoryEntity> categoryEntities = gmallPmsApi.queryCatesL123ByL3Cid(categoryId).getData();
        if (!CollectionUtils.isEmpty(categoryEntities))
            itemVo.setCategoryEntities(categoryEntities);

//        2.查品牌 V 2
        Long brandId = skuEntity.getBrandId();
        BrandEntity brandEntity = gmallPmsApi.queryBrandById(brandId).getData();
        if (brandEntity != null) {
            itemVo.setBrandId(brandId);
            itemVo.setBrandName(brandEntity.getName());
        }
//        3.查Spu信息 V 2 拿到spuId才能查哈
        Long spuId = skuEntity.getSpuId();
        SpuEntity spuEntity = gmallPmsApi.querySpuById(spuId).getData();
        if (spuEntity != null) {
            itemVo.setSpuId(spuId);
            itemVo.setSpuName(spuEntity.getName());
        }

//        4.查Sku信息 V 1
        itemVo.setSkuId(skuId);
        itemVo.setTitle(skuEntity.getTitle());
        itemVo.setSubTitle(skuEntity.getSubtitle());
        itemVo.setDefaultImage(skuEntity.getDefaultImage());
        itemVo.setPrice(skuEntity.getPrice());
        itemVo.setWeight(skuEntity.getWeight());

//        5.查促销 V 1
        List<ItemSalesVo> itemSalesVos = gmallSmsApi.allSales(skuId).getData();
        if (!CollectionUtils.isEmpty(itemSalesVos))
            itemVo.setSales(itemSalesVos);

//        6.是否有货 V 1
        List<WareSkuEntity> wareSkuEntities = gmallWmsApi.queryWareSkuBySid(skuId).getData();
        if (CollectionUtils.isEmpty(wareSkuEntities))
            itemVo.setStore(false);
        else {
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> {
                return wareSkuEntity.getStock() > 0 && wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0;
            }));
        }
//        7.一个Spu下的所有Sku的销售属性 V 2
        List<SaleAttrValueVo> saleAttrValueVos = gmallPmsApi.querySaleAttrsBySpuId(spuId).getData();
        if (!CollectionUtils.isEmpty(saleAttrValueVos))
            itemVo.setSaleAttrs(saleAttrValueVos);
//        8.sku所有的图片	images V 1
        List<SkuImagesEntity> skuImagesEntities = gmallPmsApi.querySkuImagesBySKuId(skuId).getData();
        if (!CollectionUtils.isEmpty(skuImagesEntities))
            itemVo.setImages(skuImagesEntities);
//        9.当前sku具有的销售属性 V 为了红框标注 1
        Map<Long, String> saleAttr = gmallPmsApi.querySaleAttrBySkuId(skuId).getData();
        if (!CollectionUtils.isEmpty(saleAttr))
            itemVo.setSaleAttr(saleAttr);
//        10.查映射关系 {白天白,8G,128: 10, 白天白,12G,256G: 20} attrValue : skuId 为了做页面跳转 V 2
        String mapping = gmallPmsApi.queryMappingBySpuId(spuId).getData();
        if (StringUtils.isNotBlank(mapping))
            itemVo.setMapping(mapping);
//        11.根据spuId查spu详细信息 V 2
        SpuDescEntity spuDescEntity = gmallPmsApi.querySpuDescById(spuId).getData();
        if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {
            itemVo.setSpuDesc(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
        }
//        12.查询当前spu和sku所有分组属性 V 2
        List<ItemGroupVo> itemGroupVos = gmallPmsApi.queryGroupAttrsByCidSpuIdSkuId(categoryId, spuId, skuId).getData();
        if (!CollectionUtils.isEmpty(itemGroupVos))
            itemVo.setGroups(itemGroupVos);

        return itemVo;
    }

    public void generateHtml(ItemVo itemVo){
        try (PrintWriter writer = new PrintWriter(new File("D:/Project/html/"+itemVo.getSkuId()+".html"))) {
            Context context = new Context();
            //传递数据
            context.setVariable("itemVo", itemVo);
            templateEngine.process("item", context, writer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

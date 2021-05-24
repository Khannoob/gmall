package edu.sysu.gmall.search;

import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.api.GmallPmsApi;
import edu.sysu.gmall.pms.entity.*;
import edu.sysu.gmall.search.feign.GmallPmsClient;
import edu.sysu.gmall.search.feign.GmallWmsClient;
import edu.sysu.gmall.search.pojo.Goods;
import edu.sysu.gmall.search.pojo.SearchAttrValue;
import edu.sysu.gmall.search.repository.GoodsRepository;
import edu.sysu.gmall.wms.api.GmallWmsApi;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    ElasticsearchRestTemplate restTemplate;
    @Autowired
    GoodsRepository repository;
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    GmallWmsClient gmallWmsClient;

    @Test
    void saveAll() {
        restTemplate.deleteIndex(Goods.class);
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        //Spu 返回的是一个Page对象
        Integer pageNum = 1;
        Integer pageSize = 100;
            ResponseVo<List<SpuEntity>> spuEntityResponseVo = gmallPmsClient.querySpuByPageSearch(new PageParamVo(pageNum, pageSize, null));
        do {
            List<SpuEntity> spuEntities = spuEntityResponseVo.getData();
            if (!CollectionUtils.isEmpty(spuEntities)) {
                spuEntities.forEach(spuEntity -> {
                    ResponseVo<List<SkuEntity>> skuEntityResponseVo = gmallPmsClient.querySkusBySpu(spuEntity.getId());
                    List<SkuEntity> skuEntities = skuEntityResponseVo.getData();
                    if (!CollectionUtils.isEmpty(skuEntities)) {
                        List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                            Goods goods = new Goods();
                            // skuEntity->skuId title subtitle 默认图片 价格
                            goods.setSkuId(skuEntity.getId());
                            goods.setTitle(skuEntity.getTitle());
                            goods.setSubTitle(skuEntity.getSubtitle());
                            goods.setDefaultImage(skuEntity.getDefaultImage());
                            goods.setPrice(skuEntity.getPrice().doubleValue());


                            //spuEntity->创建时间 skuWareEntity->库存 / 销量
                            goods.setCreateTime(spuEntity.getCreateTime());
                            ResponseVo<List<WareSkuEntity>> wareSkuEntityResponseVo = gmallWmsClient.queryWareSkuBySid(skuEntity.getId());

                            List<WareSkuEntity> wareSkuEntities = wareSkuEntityResponseVo.getData();
                            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                                Long sales = wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce(Long::sum).get();
                                goods.setSales(sales);
                                boolean store = wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                                        wareSkuEntity.getStock() > 0 && wareSkuEntity.getStock() > wareSkuEntity.getStockLocked()
                                );
                                goods.setStore(store);
                            }
                            //brand->品牌名/id/logo category->分类名/id
                            ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsClient.queryBrandById(spuEntity.getBrandId());
                            BrandEntity brandEntity = brandEntityResponseVo.getData();
                            if (brandEntity != null) {
                                goods.setBrandId(brandEntity.getId());
                                goods.setBrandName(brandEntity.getName());
                                goods.setLogo(brandEntity.getLogo());
                            }
                            ResponseVo<CategoryEntity> categoryEntityResponseVo = gmallPmsClient.queryCategoryById(spuEntity.getCategoryId());
                            CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                            if (categoryEntity != null) {
                                goods.setCategoryId(categoryEntity.getId());
                                goods.setCategoryName(categoryEntity.getName());
                            }

                            //List<SearchAttrValue> = List<SpuAttrValueEntity> + List<SkuAttrValueEntity>
                            ArrayList<SearchAttrValue> searchAttrValues = new ArrayList<>();
                            //查SpuAttrValueEntity
                            ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo =
                                    gmallPmsClient.queryAttrValueBySpuIdAndCid(spuEntity.getId(), spuEntity.getCategoryId());
                            List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                            if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                                List<SearchAttrValue> spuSearchAttrValues = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    SearchAttrValue attrValue = new SearchAttrValue();
                                    BeanUtils.copyProperties(spuAttrValueEntity, attrValue);
                                    return attrValue;
                                }).collect(Collectors.toList());
                                searchAttrValues.addAll(spuSearchAttrValues);
                            }
                            //查SkuAttrValueEntity
                            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo =
                                    gmallPmsClient.querySkuAttrValueEntityBySkuIdAndCid(skuEntity.getId(), skuEntity.getCategoryId());
                            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                                List<SearchAttrValue> skuSearchAttrValues = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    SearchAttrValue attrValue = new SearchAttrValue();
                                    BeanUtils.copyProperties(skuAttrValueEntity, attrValue);
                                    return attrValue;
                                }).collect(Collectors.toList());
                                searchAttrValues.addAll(skuSearchAttrValues);
                            }
                            goods.setSearchAttrs(searchAttrValues);
                            return goods;
                        }).collect(Collectors.toList());
                        repository.saveAll(goodsList);
                    }
                });
            }
            pageNum++;
            pageSize = spuEntities.size();
            if (pageSize==0)
                return;
        }while (pageSize==100);
    }

    @Test
    void contextLoads() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
    }

}

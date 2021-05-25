package edu.sysu.gmall.search.listener;

import com.rabbitmq.client.Channel;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.entity.*;
import edu.sysu.gmall.search.feign.GmallPmsClient;
import edu.sysu.gmall.search.feign.GmallWmsClient;
import edu.sysu.gmall.search.pojo.Goods;
import edu.sysu.gmall.search.pojo.SearchAttrValue;
import edu.sysu.gmall.search.repository.GoodsRepository;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-24 19:53
 */
@Component
public class GoodsListener {
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    GmallWmsClient gmallWmsClient;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    GoodsRepository repository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("SEARCH_INSERT_QUEUE"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert"}
    ))
    //插入一个Sku 更新一个Spu对应的所有Sku 即把所有Sku都查出来 然后重新放入ES
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        SpuEntity spuEntity = gmallPmsClient.querySpuById(spuId).getData();
        try {
            if (spuEntity != null) {
                ResponseVo<List<SkuEntity>> skuEntityResponseVo = gmallPmsClient.querySkusBySpu(spuId);
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
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

package edu.sysu.gmall.item.vo;

import edu.sysu.gmall.pms.entity.CategoryEntity;
import edu.sysu.gmall.pms.entity.SkuImagesEntity;
import edu.sysu.gmall.pms.vo.ItemGroupVo;
import edu.sysu.gmall.pms.vo.SaleAttrValueVo;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-29 08:52
 */
@Data
public class ItemVo {
    //分类相关
    private List<CategoryEntity> categoryEntities;

    //品牌相关
    private Long brandId;
    private String brandName;

    //Spu相关
    private String spuName;
    private Long spuId;

    //Sku相关
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;
    //Sku所有图片
    private List<SkuImagesEntity> images;

    //Sms促销相关
    private List<ItemSalesVo> sales;

    //Wms是否有货
    private Boolean store;

    //一个Spu下的所有Sku的销售属性[{3:[黑色,白色]},{4:[8G]},{5:[256G,512G]}]
    private List<SaleAttrValueVo> saleAttrs;

    //当前Sku所包含的销售属性 为了红框显示 attrId:attrValue {3:黑色,4:8G,5:256G}
    private Map<Long,String> saleAttr;

    //所有 销售属性 : skuId  的映射关系 为了选择的时候传入参数解析成skuId跳转 {白天白,8G,128: 10, 白天白,12G,256G: 20}attrValue : skuId
    private String mapping;

    //当前spu共有的商品属性 在详情页 分组展示
    private List<ItemGroupVo> groups;

    //spu的描述信息
    private List<String> spuDesc;
}

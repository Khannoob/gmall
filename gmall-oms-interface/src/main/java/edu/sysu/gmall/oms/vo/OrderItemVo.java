package edu.sysu.gmall.oms.vo;

import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-05 22:35
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String defaultImage;
    private String title;
    private List<SkuAttrValueEntity> saleAttrs; // 销售属性：List<SkuAttrValueEntity>的json格式
    private BigDecimal price; // 商品当前价格
    private BigDecimal count;
    private Boolean store = false; // 是否有货
    private List<ItemSalesVo> sales; // 营销信息: List<ItemSalesVo>的json格式
    private Integer weight;
}

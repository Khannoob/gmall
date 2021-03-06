package edu.sysu.gmall.pms.vo;

import edu.sysu.gmall.pms.entity.SkuAttrValueEntity;
import edu.sysu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-15 10:18
 */
@Data
public class SkuVo extends SkuEntity {
    private List<String> images;
    private List<SkuAttrValueEntity> saleAttrs;
    //优惠券信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //满减信息
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer addFullOther;

    //多买多送
    private Integer fullCount;
    private BigDecimal discount;
    private Integer addLadderOther;
}

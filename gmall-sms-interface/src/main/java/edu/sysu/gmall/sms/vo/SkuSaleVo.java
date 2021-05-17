package edu.sysu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-16 23:03
 */
@Data
public class SkuSaleVo {
    private Long skuId;
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

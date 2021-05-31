package edu.sysu.gmall.pms.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-29 11:29
 */
@Data
public class SaleAttrValueVo {
    //销售属性的id
    private Long attrId;
    //销售属性的name
    private String attrName;
    //销售属性的值
    private Set<String> attrValues;
}

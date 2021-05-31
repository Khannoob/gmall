package edu.sysu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-29 11:49
 */
@Data
public class ItemGroupVo {
    //分组的名
    private String groupName;
    //当前组拥有的属性
    private List<AttrValueVo> attrValues;
}

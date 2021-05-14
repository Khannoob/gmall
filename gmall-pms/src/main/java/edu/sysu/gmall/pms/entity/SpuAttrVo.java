package edu.sysu.gmall.pms.entity;

import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-14 21:02
 */
//@Data
public class SpuAttrVo extends SpuAttrValueEntity{
    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        this.valueSelected = valueSelected;
    }
}

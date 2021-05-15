package edu.sysu.gmall.pms.vo;

import edu.sysu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-14 21:02
 */
//@Data
public class SpuAttrVo extends SpuAttrValueEntity {
//    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        if(CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}

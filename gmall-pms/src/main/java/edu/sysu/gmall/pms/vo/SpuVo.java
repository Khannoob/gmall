package edu.sysu.gmall.pms.vo;

import edu.sysu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-14 20:55
 */
@Data
public class SpuVo extends SpuEntity {
    private List<String> spuImages;
    private List<SpuAttrVo> baseAttrs;
    private List<SkuVo> skus;
}

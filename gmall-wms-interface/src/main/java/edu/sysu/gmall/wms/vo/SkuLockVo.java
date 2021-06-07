package edu.sysu.gmall.wms.vo;

import lombok.Data;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-06 23:22
 */
@Data
public class SkuLockVo {
    private Integer count;
    private Long skuId;
    private Boolean lock;
    private Long wareSkuId;
}

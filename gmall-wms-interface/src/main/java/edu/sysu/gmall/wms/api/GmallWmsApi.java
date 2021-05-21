package edu.sysu.gmall.wms.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-19 18:56
 */
public interface GmallWmsApi {
    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuBySid(@PathVariable Long skuId);
}

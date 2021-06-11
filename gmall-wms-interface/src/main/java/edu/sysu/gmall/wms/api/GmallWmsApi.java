package edu.sysu.gmall.wms.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import edu.sysu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-19 18:56
 */
public interface GmallWmsApi {
    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuBySid(@PathVariable Long skuId);
    @PostMapping("wms/waresku/check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkLock(@PathVariable String orderToken, @RequestBody List<SkuLockVo> lockVos);
}

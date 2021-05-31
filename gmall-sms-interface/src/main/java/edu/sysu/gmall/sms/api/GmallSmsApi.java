package edu.sysu.gmall.sms.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import edu.sysu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-16 23:06
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sales/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo);

    @GetMapping("sms/skubounds/all/{skuId}")
    public ResponseVo<List<ItemSalesVo>> allSales(@PathVariable Long skuId);
}

package edu.sysu.gmall.sms.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-16 23:06
 */
public interface SmsFeignClient {
    @PostMapping("sms/skubounds/sales/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo);
}

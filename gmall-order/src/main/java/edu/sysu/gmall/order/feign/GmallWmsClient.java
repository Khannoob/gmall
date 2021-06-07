package edu.sysu.gmall.order.feign;

import edu.sysu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-19 19:00
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}

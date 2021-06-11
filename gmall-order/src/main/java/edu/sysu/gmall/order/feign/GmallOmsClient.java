package edu.sysu.gmall.order.feign;

import edu.sysu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-07 18:44
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}

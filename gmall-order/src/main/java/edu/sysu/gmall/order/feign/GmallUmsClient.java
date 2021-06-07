package edu.sysu.gmall.order.feign;

import edu.sysu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-06 17:52
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}

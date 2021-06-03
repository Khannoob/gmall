package edu.sysu.gmall.auth.feign;

import edu.sysu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 15:08
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}

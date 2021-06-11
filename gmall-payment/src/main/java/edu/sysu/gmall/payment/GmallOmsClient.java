package edu.sysu.gmall.payment;

import edu.sysu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-08 11:40
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}

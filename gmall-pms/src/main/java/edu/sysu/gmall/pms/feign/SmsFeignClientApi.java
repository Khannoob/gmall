package edu.sysu.gmall.pms.feign;

import edu.sysu.gmall.sms.api.SmsFeignClient;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-17 00:03
 */
@FeignClient("sms-service")
public interface SmsFeignClientApi extends SmsFeignClient {
}

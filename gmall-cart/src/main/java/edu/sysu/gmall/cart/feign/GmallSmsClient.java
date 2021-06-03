package edu.sysu.gmall.cart.feign;

import edu.sysu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-29 16:41
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}

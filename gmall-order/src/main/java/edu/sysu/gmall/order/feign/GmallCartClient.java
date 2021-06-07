package edu.sysu.gmall.order.feign;

import edu.sysu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-06 17:06
 */
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {

}

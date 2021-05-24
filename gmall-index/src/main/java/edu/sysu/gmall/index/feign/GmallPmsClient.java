package edu.sysu.gmall.index.feign;

import edu.sysu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-24 18:28
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}

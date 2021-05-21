package edu.sysu.gmall.search.feign;

import edu.sysu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-19 18:50
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}

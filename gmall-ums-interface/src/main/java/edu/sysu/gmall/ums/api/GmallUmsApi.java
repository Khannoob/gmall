package edu.sysu.gmall.ums.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.ums.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 11:30
 */
public interface GmallUmsApi {
    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(@RequestParam String loginName,@RequestParam String password);
}

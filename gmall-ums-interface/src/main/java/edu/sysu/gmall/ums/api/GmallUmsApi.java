package edu.sysu.gmall.ums.api;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.ums.entity.UserAddressEntity;
import edu.sysu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 11:30
 */
public interface GmallUmsApi {
    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(@RequestParam String loginName,@RequestParam String password);
    @GetMapping("ums/user/{id}")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);
    @GetMapping("ums/useraddress/user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryUserAddressByUserId(@PathVariable String userId);
}

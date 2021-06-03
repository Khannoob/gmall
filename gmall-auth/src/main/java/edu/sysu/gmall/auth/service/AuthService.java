package edu.sysu.gmall.auth.service;

import edu.sysu.gmall.auth.config.JwtProperties;
import edu.sysu.gmall.auth.exception.UserException;
import edu.sysu.gmall.auth.feign.GmallUmsClient;
import edu.sysu.gmall.common.utils.CookieUtils;
import edu.sysu.gmall.common.utils.IpUtil;
import edu.sysu.gmall.common.utils.JwtUtils;
import edu.sysu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 12:44
 */
@Service
@EnableFeignClients
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    GmallUmsClient gmallUmsClient;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            //1.查询用户是否存在
            UserEntity userEntity = gmallUmsClient.queryUser(loginName, password).getData();
            if (userEntity == null) {
                throw new UserException("用户名或者密码错误");
            }
            //2.获取用户登陆ip 防盗用
            String ip = IpUtil.getIpAddressAtService(request);
            //3.封装用户信息 载荷 map(加上用户ip)
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("username", userEntity.getUsername());
            map.put("ip", ip);
            //4.生成jwt
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpireMinutes());
            //5.设置jwt到cookie中
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token,
                    jwtProperties.getExpireMinutes() * 60);
            //6.把用户的昵称再放入另外一个cookie unick
            CookieUtils.setCookie(request, response, jwtProperties.getUnick(), userEntity.getNickname(),
                    jwtProperties.getExpireMinutes() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

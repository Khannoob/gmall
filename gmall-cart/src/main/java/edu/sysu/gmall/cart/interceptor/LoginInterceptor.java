package edu.sysu.gmall.cart.interceptor;

import edu.sysu.gmall.cart.config.JwtProperties;
import edu.sysu.gmall.cart.pojo.UserInfo;
import edu.sysu.gmall.common.utils.CookieUtils;
import edu.sysu.gmall.common.utils.JwtUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-02 15:25
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在cookie中获取唯一标识 user-key 若不存在就创建一个
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());
        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, jwtProperties.getUserKey(), userKey
                    , jwtProperties.getExpire());
        }
        //在cookie中获取GMALL_TOKEN 若不存在返回(userKey,null)
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)) {
            //同一个请求执行流程使用的是同一个线程
            THREAD_LOCAL.set(new UserInfo(userKey, null));
            return true;
        }
        //对token进行解析 返回(userKey,userId)
        try {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            String userId = map.get("userId").toString();
            THREAD_LOCAL.set(new UserInfo(userKey, userId));
        } catch (Exception e) {
            e.printStackTrace();
            //出现异常说明解析失败 返回(userKey,null)
            THREAD_LOCAL.set(new UserInfo(userKey, null));
            return true;
        }
        return true;
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        System.out.println("后置处理...");
//    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //必须移除掉ThreadLocalMap中的值,否则因为tomcat使用了线程池会导致线程不死亡 ThreadLocalMap值一直存在 内存泄露 Memory Leak
        THREAD_LOCAL.remove();
    }
}

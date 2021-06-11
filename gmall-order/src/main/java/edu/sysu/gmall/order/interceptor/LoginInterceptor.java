package edu.sysu.gmall.order.interceptor;

import edu.sysu.gmall.cart.pojo.UserInfo;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-02 15:25
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("userId");
        THREAD_LOCAL.set(new UserInfo(null, userId));
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

package edu.sysu.gmall.gateway.filter;

import edu.sysu.gmall.common.utils.IpUtil;
import edu.sysu.gmall.common.utils.JwtUtils;
import edu.sysu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 20:15
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PathGatewayFilterFactory extends AbstractGatewayFilterFactory<PathGatewayFilterFactory.PathConfig> {
    @Autowired
    JwtProperties jwtProperties;

    //1.继承AbstractGatewayFilterFactory 重写GatewayFilter 放行chan.filter(exchange)
    @Override
    public GatewayFilter apply(PathConfig pathConfig) {
        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
            //判断当前服务是否在拦截路径内(filters的参数中) 不在就直接放行
            List<String> paths = pathConfig.getPaths();
            if (CollectionUtils.isEmpty(paths)) {
                return chain.filter(exchange);
            }
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String curPath = request.getURI().getPath();
            boolean flag = paths.stream().anyMatch(path -> {
                return StringUtils.startsWith(curPath, path);
            });
            //都不满足直接放行
            if (!flag) {
                return chain.filter(exchange);
            }
            //只要当前path满足开头是其中一个配置参数的就要进行拦截 进行token的解析和判断
            //1.看看token是否存在于请求头
            String token = request.getHeaders().getFirst("token");
            if (StringUtils.isBlank(token)) {
                //如果请求头没有token就去cookie看看有没有token
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if (CollectionUtils.isEmpty(cookies))
                    return redirect(request, response);
                token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                if (StringUtils.isBlank(token)) {
                    return redirect(request, response);
                }
            }
            //有token对token进行jwt解析
            try {
                //获取有效载荷
                Map<String, Object> payLoad = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                //判断ip是否一致 不一致也重定向
                String ip = payLoad.get("ip").toString();
                String curIp = IpUtil.getIpAddressAtGateway(request);
                if (!StringUtils.equals(curIp, ip))
                    return redirect(request, response);
                //为减少jwt的重复解析 我们把解析好的用户id放到请求头
                String userId = payLoad.get("userId").toString();
                request.mutate().header("userId", userId).build();
                exchange.mutate().request(request).build();
            } catch (Exception e) {
                e.printStackTrace();
                //解析失败说明token过期 重定向到登录页面
                return redirect(request, response);
            }
            return chain.filter(exchange);
        };
    }

    //重定向到登录页的方法
    private Mono<Void> redirect(ServerHttpRequest request, ServerHttpResponse response) {
        //token还是空 直接重定向去登陆页面
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
        return response.setComplete();
    }

    //2.指定参数内部类,并声明在AbstractGatewayFilterFactory的泛型
    @Data
    public static final class PathConfig {
        private List<String> paths;
    }

    //3.重写构造方法
    public PathGatewayFilterFactory() {
        super(PathConfig.class);
    }
    //4.指定参数的顺序(一个参数应该可以不指定了..... 不行 一个参数也要指定排序) 重写 shortcutFieldOrder()

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    //5.指定参数的类型 重写shortcutType(ShortcutType.GATHER_LIST)

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }
}

package edu.sysu.gmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 18:51
 */
@Component
public class MyGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("全局过滤器,无差别的拦截所有经过网关的请求......123");
//        System.out.println(exchange.getRequest().getHeaders().get("aaa"));
//        System.out.println(exchange.getResponse().getHeaders().get("aaa"));
        return chain.filter(exchange);
    }
}

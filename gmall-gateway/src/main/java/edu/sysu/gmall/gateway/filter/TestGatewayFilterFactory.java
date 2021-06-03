package edu.sysu.gmall.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 19:23
 */
@Component
public class TestGatewayFilterFactory extends AbstractGatewayFilterFactory<TestGatewayFilterFactory.TestConfig> {


    //继承AbstractGatewayFilterFactory类实现自定义的局部过滤器
    @Override
    public GatewayFilter apply(TestConfig config) {
        return (exchange,chain)->{
            System.out.println("这是局部过滤器,只拦截配置了filters的路由.........");
            System.out.println("参数:"+config.parameters);
            return chain.filter(exchange);
        };
    }



    //1.通过内部类 获取配置文件filters的参数(指定2个参数)
    @Data
    public static class TestConfig{
        private List<String> parameters;
    }
    //2.重写构造器,把内部类往父类方法里面传
    public TestGatewayFilterFactory() {
        super(TestConfig.class);
    }
    //3.指定2个参数的顺序(key和value的顺序) 重写shortcutFieldOrder方法
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("parameters");
    }
    //4.如果想指定不限制个数的filters 重写shortcutType方法 指定为List

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }
}

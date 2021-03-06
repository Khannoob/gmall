package edu.sysu.gmall.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients
@MapperScan("edu.sysu.gmall.cart.mapper")
@EnableAsync
@EnableSwagger2
public class GmallCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallCartApplication.class, args);
    }

}

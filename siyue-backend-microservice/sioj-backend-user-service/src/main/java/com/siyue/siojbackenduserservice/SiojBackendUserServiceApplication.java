package com.siyue.siojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.siyue.siojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.siyue")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.siyue.siojbackendserviceclient.service"})
public class SiojBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiojBackendUserServiceApplication.class, args);
    }

}

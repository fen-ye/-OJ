package com.siyue.siojbackendmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SiojBackendModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiojBackendModelApplication.class, args);
    }

}

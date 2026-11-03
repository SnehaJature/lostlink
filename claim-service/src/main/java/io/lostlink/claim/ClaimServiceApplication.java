package io.lostlink.claim;

import io.lostlink.common.security.CommonSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import(CommonSecurityConfiguration.class)
public class ClaimServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClaimServiceApplication.class, args);
    }
}

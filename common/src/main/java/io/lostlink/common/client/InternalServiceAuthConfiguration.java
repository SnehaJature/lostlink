package io.lostlink.common.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class InternalServiceAuthConfiguration {
    @Bean
    RequestInterceptor internalServiceKey(@Value("${INTERNAL_SERVICE_KEY}") String serviceKey) {
        return template -> template.header("X-Internal-Service-Key", serviceKey);
    }
}

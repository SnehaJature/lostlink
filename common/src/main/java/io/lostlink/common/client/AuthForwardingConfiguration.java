package io.lostlink.common.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthForwardingConfiguration {
    @Bean
    RequestInterceptor forwardBearerToken() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof String token) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}

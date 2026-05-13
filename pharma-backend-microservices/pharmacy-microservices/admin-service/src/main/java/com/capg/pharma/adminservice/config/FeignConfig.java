package com.capg.pharma.adminservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.RequestContextFilter;

/**
 * Feign configuration that forwards the incoming JWT Authorization header
 * to all downstream service calls made by the Admin Service.
 *
 * <p>Uses inheritable thread-local via RequestContextFilter so the token
 * is available in Feign's execution thread.</p>
 */
@Configuration
public class FeignConfig {

    /**
     * Override the default RequestContextFilter to use inheritable thread-locals.
     * This makes the request context available in child threads (Feign calls).
     */
    @Bean
    public RequestContextFilter requestContextFilter() {
        RequestContextFilter filter = new RequestContextFilter();
        filter.setThreadContextInheritable(true);
        return filter;
    }

    @Bean
    public RequestInterceptor forwardAuthorizationHeader() {
        return requestTemplate -> {
            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    String authHeader = attrs.getRequest().getHeader("Authorization");
                    if (authHeader != null) {
                        requestTemplate.header("Authorization", authHeader);
                    }
                }
            } catch (Exception ignored) {}
        };
    }
}

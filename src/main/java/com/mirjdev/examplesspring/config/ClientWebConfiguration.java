package com.mirjdev.examplesspring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration(proxyBeanMethods = false)
public class ClientWebConfiguration {

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer clientPageSizeLimit() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}

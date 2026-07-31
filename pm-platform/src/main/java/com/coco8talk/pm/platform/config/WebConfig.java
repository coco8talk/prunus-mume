package com.coco8talk.pm.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web配置类，处理跨域等请求
 *
 * @author coco8talk
 * @since 2025/7/3 19:04
 **/
@Configuration
@EnableConfigurationProperties(WebProperties.class)
public class WebConfig implements WebMvcConfigurer {
    private final List<String> allowedOrigins;

    public WebConfig(WebProperties properties) {
        this.allowedOrigins = properties.allowedOrigins();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT")
                .allowedHeaders("*")
                .exposedHeaders("satoken")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

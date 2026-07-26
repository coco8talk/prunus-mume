package com.coco8talk.pm.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，处理跨域等请求
 *
 * @author coco8talk
 * @since 2025/7/3 19:04
 **/
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

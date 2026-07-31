package com.coco8talk.pm.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Web 配置属性
 * @param allowedOrigins `
 */
@ConfigurationProperties(prefix = "coco8talk.web")
public record WebProperties(List<String> allowedOrigins) {

    public WebProperties {
        allowedOrigins = allowedOrigins == null
                ? List.of()
                : List.copyOf(allowedOrigins);
    }
}
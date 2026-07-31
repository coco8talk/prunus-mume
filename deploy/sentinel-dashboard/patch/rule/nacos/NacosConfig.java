/*
 * Sentinel Dashboard Nacos datasource extension.
 * Wires a shared Nacos ConfigService bean, configured from container env vars
 * (NACOS_SERVER_ADDR / NACOS_NAMESPACE / NACOS_AUTH_USERNAME / NACOS_AUTH_PASSWORD).
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import java.util.Properties;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class NacosConfig {

    @Value("${NACOS_SERVER_ADDR:nacos:8848}")
    private String serverAddr;
    @Value("${NACOS_NAMESPACE:}")
    private String namespace;
    @Value("${NACOS_AUTH_USERNAME:}")
    private String username;
    @Value("${NACOS_AUTH_PASSWORD:}")
    private String password;

    @Bean
    public ConfigService nacosConfigService() throws NacosException {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        if (StringUtils.hasText(namespace)) {
            properties.setProperty("namespace", namespace);
        }
        if (StringUtils.hasText(username)) {
            properties.setProperty("username", username);
            properties.setProperty("password", password);
        }
        return NacosFactory.createConfigService(properties);
    }
}

package com.coco8talk.pm.auth;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author coco8talk
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能 
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}

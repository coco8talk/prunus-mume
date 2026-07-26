package com.coco8talk.pm.platform.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ConstantDelay;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Redisson配置类
 * 用于在Spring环境下配置Redisson客户端
 *
 * @author coco8talk
 * @since 2025/6/2 22:59
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {
    
    private String host;
    private Integer port;
    private Integer database;
    
    /**
     * 创建并配置Redisson客户端
     * <p>
     * 此方法内部定义了如何创建和配置Redisson客户端的实例
     * 它使用单服务器配置，设置了Redis服务器的地址和数据库索引
     *
     * @return RedissonClient Redisson客户端实例，已配置好连接到指定的Redis服务器
     */
    @Bean
    public RedissonClient redissonClient() {
        // 创建配置对象
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        // 配置使用单个服务器的连接方式
        config.useSingleServer()
              // 设置Redis服务器的地址
              .setAddress(redisAddress)
              // 设置要使用的数据库索引
              .setDatabase(database)
              // 固定重试延迟：Redisson 3.49 用 getRetryDelay().calcDelay(0) 推导 Netty
              // 定时器的 tickDuration，默认抖动策略是随机值，偶尔会使 tickDuration 算成 0
              // （HashedWheelTimer 要求 > 0），导致启动间歇性失败。固定为 1s 后
              // min(delay, timeout)=1000 -> tickDuration=100，结果确定。
              .setRetryDelay(new ConstantDelay(Duration.ofSeconds(1)));
        
        // 使用配置创建并返回Redisson客户端实例
        return Redisson.create(config);
    }
    
}

package com.coco8talk.pm.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置类
 * @author coco8talk
 * @since 2025/7/15 17:59
 **/
@Configuration
public class ThreadPoolConfig {
    
    /**
     * 创建一个线程池
     * @return ExecutorService
     */
    @Bean("executorService")
    public ExecutorService executorService() {
        //核心线程数
        int poolSize = 24;
        //最大线程数
        int maxPoolSize = 48;
        //空闲线程存活时间
        long keepAliveSeconds = 60L;
        //工作队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(5000);
        
        //创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                workQueue,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        //设置线程池名称前缀
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    
        return executor;
    }
}

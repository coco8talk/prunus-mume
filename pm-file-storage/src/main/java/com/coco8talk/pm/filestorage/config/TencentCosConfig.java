package com.coco8talk.pm.filestorage.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author coco8talk
 * @since 2025/10/16 19:55
 */
@Log4j2
@Configuration // 标记为配置类
public class TencentCosConfig {

    @Value("${coco8talk.tencent.cos.secretId}")
    private String secretId;
    
    @Value("${coco8talk.tencent.cos.secretKey}")
    private String secretKey;
    
    @Value("${coco8talk.tencent.cos.region}")
    private String region;

    /**
     * 创建COS客户端Bean（单例模式）
     */
    @Bean(destroyMethod = "shutdown")
    public COSClient cosClient() {
        // 1. 创建凭证
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        
        // 2. 配置客户端
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 可选：设置连接池等高级参数
        // clientConfig.setHttpProtocol(HttpProtocol.HTTPS);
        // clientConfig.setConnectionPoolSize(30);
        
        // 3. 创建客户端实例
        COSClient client = new COSClient(cred, clientConfig);
        
        log.info("Tencent COS Client initialized for region: {}", region);
        return client;
    }
}

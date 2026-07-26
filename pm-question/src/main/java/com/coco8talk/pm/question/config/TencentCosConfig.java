package com.coco8talk.pm.question.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 题库封面上传所需的腾讯云 COS 客户端配置。
 */
@Configuration
public class TencentCosConfig {

    @Value("${coco8talk.tencent.cos.secretId}")
    private String secretId;

    @Value("${coco8talk.tencent.cos.secretKey}")
    private String secretKey;

    @Value("${coco8talk.tencent.cos.region}")
    private String region;

    @Bean(destroyMethod = "shutdown")
    public COSClient cosClient() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        return new COSClient(credentials, new ClientConfig(new Region(region)));
    }
}

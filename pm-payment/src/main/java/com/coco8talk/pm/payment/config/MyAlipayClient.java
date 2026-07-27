package com.coco8talk.pm.payment.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.BizException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Alipay Configuration
 *
 * @author coco8talk
 * @since 2025/10/20 22:59
 **/
@Configuration
@Log4j2
public class MyAlipayClient {
    @Value("${coco8talk.alipay.app-id}")
    private String appId;
    @Value("${coco8talk.alipay.gateway-url:https://openapi-sandbox.dl.alipaydev.com/gateway.do}")
    private String gatewayUrl;
    @Value("${coco8talk.alipay.alipayPublicKey}")
    private String alipayPublicKey;
    @Value("${coco8talk.alipay.privateKey}")
    private String privateKey;
    
    @Bean
    public AlipayClient alipayClient() {
        AlipayConfig alipayConfig = getAlipayConfig(appId, privateKey, alipayPublicKey, gatewayUrl);
        try {
            return new DefaultAlipayClient(alipayConfig);
        } catch (AlipayApiException e) {
            log.error("创建 AlipayClient 失败", e);
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "创建 AlipayClient 失败");
        }
    }
    
    static AlipayConfig getAlipayConfig(String appId, String privateKey, String alipayPublicKey) {
        return getAlipayConfig(appId, privateKey, alipayPublicKey, "https://openapi-sandbox.dl.alipaydev.com/gateway.do");
    }

    static AlipayConfig getAlipayConfig(String appId, String privateKey, String alipayPublicKey, String gatewayUrl) {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(gatewayUrl);
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }
}

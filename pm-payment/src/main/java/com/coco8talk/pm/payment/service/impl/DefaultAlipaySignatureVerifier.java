package com.coco8talk.pm.payment.service.impl;

import com.alipay.api.internal.util.AlipaySignature;
import com.coco8talk.pm.payment.service.AlipaySignatureVerifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Log4j2
public class DefaultAlipaySignatureVerifier implements AlipaySignatureVerifier {
    @Value("${coco8talk.alipay.alipayPublicKey}")
    private String alipayPublicKey;

    @Override
    public boolean verify(Map<String, String> params) {
        try {
            log.info("支付宝验签开始");
            log.info("支付宝验签参数：{}", params);
            log.info("支付宝验签公钥：{}", alipayPublicKey);
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        } catch (Exception e) {
            log.error("支付宝验签异常", e);
            return false;
        }
    }
}

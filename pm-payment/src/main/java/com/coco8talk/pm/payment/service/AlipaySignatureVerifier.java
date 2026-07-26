package com.coco8talk.pm.payment.service;

import java.util.Map;

/** 支付宝异步通知验签接口。 */
public interface AlipaySignatureVerifier {
    /** 支付宝异步通知验签。 */
    boolean verify(Map<String, String> params);
}

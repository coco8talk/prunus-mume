package com.coco8talk.pm.payment.service.impl;

import com.alipay.api.AlipayClient;
import com.coco8talk.pm.api.auth.service.AuthSessionApi;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.payment.service.AlipaySignatureVerifier;
import com.coco8talk.pm.payment.mapper.MembershipOrderMapper;
import com.coco8talk.pm.payment.mapper.MembershipPriceMapper;
import com.coco8talk.pm.payment.model.entity.MembershipOrder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipOrderServiceImplTest {

    @Mock
    private MembershipPriceMapper membershipPriceMapper;

    @Mock
    private MembershipOrderMapper membershipOrderMapper;

    @Mock
    private AlipayClient alipayClient;

    @Mock
    private UserApi userApi;

    @Mock
    private AuthSessionApi authSessionApi;

    @Mock
    private AlipaySignatureVerifier signatureVerifier;

    @Mock
    private HttpServletRequest request;

    private MembershipOrderServiceImpl membershipOrderService;

    @BeforeEach
    void setUp() {
        membershipOrderService = new MembershipOrderServiceImpl(
                membershipPriceMapper,
                membershipOrderMapper,
                alipayClient,
                userApi,
                authSessionApi,
                signatureVerifier);
        ReflectionTestUtils.setField(membershipOrderService, "appId", "app-123");
    }

    @Test
    void alipayCallbackMarksOrderPaidAndGrantsVipAfterSuccessfulVerification() {
        Map<String, String[]> parameters = callbackParameters();
        MembershipOrder order = new MembershipOrder();
        order.setUserId(7L);
        order.setOutOrderNo("ORDER-42");
        order.setAmount(new BigDecimal("19.90"));
        order.setPaymentStatus("PENDING");
        order.setDurationType("MONTH");
        order.setDurationValue(1);
        when(request.getParameterMap()).thenReturn(parameters);
        when(signatureVerifier.verify(anyMap())).thenReturn(true);
        when(membershipOrderMapper.selectByOutTradeNo("ORDER-42")).thenReturn(order);
        when(membershipOrderMapper.selectActivePaidByUserId(7L)).thenReturn(null);

        String result = membershipOrderService.alipayCallback(request);

        assertThat(result).isEqualTo("success");
        assertThat(order.getOrderNo()).isEqualTo("ALIPAY-99");
        assertThat(order.getPaymentStatus()).isEqualTo("PAID");
        assertThat(order.getUpdatedAt()).isNotNull();
        verify(membershipOrderMapper).updateById(order);
        verify(userApi).grantVip(7L, Period.ofMonths(1));
        verify(userApi, never()).toSessionDto(7L);
        verifyNoInteractions(authSessionApi);
    }

    @Test
    void alipayCallbackReturnsFailureWithoutSideEffectsWhenSignatureIsInvalid() {
        Map<String, String[]> parameters = callbackParameters();
        when(request.getParameterMap()).thenReturn(parameters);
        when(signatureVerifier.verify(anyMap())).thenReturn(false);

        String result = membershipOrderService.alipayCallback(request);

        assertThat(result).isEqualTo("failure");
        verify(signatureVerifier).verify(Map.of(
                "app_id", "app-123",
                "trade_status", "TRADE_SUCCESS",
                "out_trade_no", "ORDER-42",
                "total_amount", "19.90",
                "trade_no", "ALIPAY-99"));
        verifyNoInteractions(membershipOrderMapper, userApi, authSessionApi);
    }

    private static Map<String, String[]> callbackParameters() {
        Map<String, String[]> parameters = new LinkedHashMap<>();
        parameters.put("app_id", new String[]{"app-123"});
        parameters.put("trade_status", new String[]{"TRADE_SUCCESS"});
        parameters.put("out_trade_no", new String[]{"ORDER-42"});
        parameters.put("total_amount", new String[]{"19.90"});
        parameters.put("trade_no", new String[]{"ALIPAY-99"});
        return parameters;
    }
}

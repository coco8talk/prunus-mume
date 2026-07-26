package com.coco8talk.pm.payment.controller;

import com.coco8talk.pm.payment.service.MembershipOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AlipayControllerTest {

    @Mock
    private MembershipOrderService membershipOrderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AlipayController(membershipOrderService)).build();
    }

    @Test
    void alipayCallbackDelegatesRequestParametersAndReturnsSuccess() throws Exception {
        when(membershipOrderService.alipayCallback(any(HttpServletRequest.class)))
                .thenReturn("success");

        mockMvc.perform(post("/alipay/callback")
                        .param("out_trade_no", "ORDER-42")
                        .param("trade_status", "TRADE_SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        ArgumentCaptor<HttpServletRequest> requestCaptor =
                ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(membershipOrderService).alipayCallback(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getParameter("out_trade_no"))
                .isEqualTo("ORDER-42");
        assertThat(requestCaptor.getValue().getParameter("trade_status"))
                .isEqualTo("TRADE_SUCCESS");
    }

    @Test
    void alipayCallbackReturnsFailureWhenServiceRejectsSignature() throws Exception {
        when(membershipOrderService.alipayCallback(any(HttpServletRequest.class)))
                .thenReturn("failure");

        mockMvc.perform(post("/alipay/callback")
                        .param("out_trade_no", "ORDER-42")
                        .param("sign", "invalid"))
                .andExpect(status().isOk())
                .andExpect(content().string("failure"));

        verify(membershipOrderService).alipayCallback(any(HttpServletRequest.class));
    }
}

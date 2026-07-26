package com.coco8talk.pm.payment.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员订单创建成功响应 VO
 *
 * @author coco8talk
 */
@Data
public class MembershipOrderCreateVO {

    /** 商户订单号（前端 outTradeNo）。 */
    private String outTradeNo;
    /** 订单支付状态（PENDING/PAID）。 */
    private String status;
    /** 应付金额。 */
    private BigDecimal amount;
    /** 货币类型。 */
    private String currency;
    /** 支付方式。 */
    private String paymentMethod;
    /** 订单创建时间。 */
    private LocalDateTime createTime;
}

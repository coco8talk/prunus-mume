package com.coco8talk.pm.payment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员订单表
 *
 * @author coco8talk
 * @TableName membership_order
 */
@TableName(value = "membership_order")
@Data
public class MembershipOrder {
    /**
     * 主键ID，雪花算法生成，全局唯一
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID，关联用户表
     */
    private Long userId;
    
    /**
     * 平台订单号，系统生成的唯一订单标识
     */
    private String outOrderNo;
    
    /**
     * 第三方支付订单号（微信/支付宝/Stripe等），支付成功后由支付平台返回
     */
    private String orderNo;
    
    /**
     * 会员等级ID，对应会员等级配置表中的等级
     */
    private Integer levelId;
    
    /**
     * 会员时长类型：MONTH（月）、QUARTER（季度）、YEAR（年）、TRIAL（试用）
     */
    private String durationType;
    
    /**
     * 会员时长数值，如1（月）、3（季）、12（年）等，单位由duration_type决定
     */
    private Integer durationValue;
    
    /**
     * 实际支付金额，单位为元
     */
    private BigDecimal amount;
    
    /**
     * 货币类型，ISO 4217标准，如CNY、USD等，默认为人民币
     */
    private String currency;
    
    /**
     * 支付状态：PENDING（待支付）、PAID（已支付）、FAILED（支付失败）、REFUNDED（已退款）
     */
    private String paymentStatus;
    
    /**
     * 支付方式：ALIPAY（支付宝）、WECHAT（微信支付）、STRIPE（Stripe）等
     */
    private String paymentMethod;
    
    /**
     * 会员服务生效开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 会员服务到期结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 记录最后更新时间
     */
    private LocalDateTime updatedAt;
}
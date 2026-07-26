package com.coco8talk.pm.payment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 会员定价配置表
 * @TableName membership_price
 */
@TableName(value ="membership_price")
@Data
public class MembershipPrice {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员等级ID（1:基础, 2:高级, 3:企业）
     */
    private Integer levelId;

    /**
     * 时长类型：MONTH/QUARTER/YEAR
     */
    private String durationType;

    /**
     * 时长数值：1（月）、3（季）、12（年）
     */
    private Integer durationValue;

    /**
     * 货币代码，ISO 4217
     */
    private String currency;

    /**
     * 定价金额
     */
    private BigDecimal amount;

    /**
     * 状态：1-启用，0-停用
     */
    private Integer status;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;
}
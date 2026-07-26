package com.coco8talk.pm.payment.model.dto;

import lombok.Data;

/**
 * 会员订单创建请求 DTO
 *
 * @author coco8talk
 */
@Data
public class MembershipOrderCreateDTO {
    
    /**
     * 会员等级ID
     */
    private Byte levelId;
    
    /**
     * 时长类型：MONTH/QUARTER/YEAR/TRIAL
     */
    private String durationType;
    
    /**
     * 时长数值：1, 3, 12 等
     */
    private Integer durationValue;
    
    /**
     * 货币类型，默认 CNY
     */
    private String currency;
}
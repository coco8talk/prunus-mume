package com.coco8talk.pm.question.bank.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库表脱敏信息返回封装类
 *
 * @author coco8talk
 * @since 2025/6/26 21:37
 **/
@Data
public class QuestionBankVO {
    
    /**
     * 题库ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 题库名称
     */
    private String title;
    
    /**
     * 题库图片
     */
    private String picture;
    
    /**
     * 创建用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    /**
     * 题库描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

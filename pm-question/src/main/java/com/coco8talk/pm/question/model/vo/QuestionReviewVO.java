package com.coco8talk.pm.question.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 题目审核VO
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@Data
public class QuestionReviewVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 题目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;
    
    /**
     * 题目标题
     */
    private String questionTitle;
    
    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId;
    
    /**
     * 审核人名称
     */
    private String reviewerName;
    
    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    private Integer reviewStatus;
    
    /**
     * 审核状态描述
     */
    private String reviewStatusDesc;
    
    /**
     * 审核意见
     */
    private String reviewMessage;
    
    /**
     * 审核时间
     */
    private Date reviewTime;
} 
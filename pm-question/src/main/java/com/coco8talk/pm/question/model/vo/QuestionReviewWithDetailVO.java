package com.coco8talk.pm.question.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目审核详情VO（包含题目详细信息）
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@Data
public class QuestionReviewWithDetailVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 审核记录ID
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
     * 题目内容
     */
    private String questionContent;
    
    /**
     * 题目答案
     */
    private String questionAnswer;
    
    /**
     * 题目标签
     */
    private List<String> questionTags;
    
    /**
     * 难度等级 1-简单 2-中等 3-困难
     */
    private Integer questionDifficulty;
    
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
    private LocalDateTime reviewTime;
    
    /**
     * 题目创建时间
     */
    private LocalDateTime questionCreateTime;
} 
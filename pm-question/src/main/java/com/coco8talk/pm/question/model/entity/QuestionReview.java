package com.coco8talk.pm.question.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目审核表
 *
 * @author coco8talk
 * @TableName question_review
 */
@TableName(value = "question_review")
@Data
public class QuestionReview {
    /**
     * 主键ID
     * -- GETTER --
     * 主键ID
     * -- SETTER --
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 题目ID
     * -- GETTER --
     * 题目ID
     * -- SETTER --
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 审核人ID
     * -- GETTER --
     * 审核人ID
     * -- SETTER --
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     * -- GETTER --
     * 审核状态 0-待审核 1-通过 2-拒绝
     * -- SETTER --
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    private Integer reviewStatus;
    
    /**
     * 审核意见
     * -- GETTER --
     * 审核意见
     * -- SETTER --
     * 审核意见
     */
    private String reviewMessage;
    
    /**
     * 审核时间
     * -- GETTER --
     * 审核时间
     * -- SETTER --
     * 审核时间
     */
    private LocalDateTime reviewTime;
}
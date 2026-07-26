package com.coco8talk.pm.question.model.dto;

import com.coco8talk.pm.question.constant.QuestionReviewConstant;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核题目DTO
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@Data
public class ReviewQuestionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    @Positive(message = "题目ID必须为正数")
    private Long questionId;
    
    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = QuestionReviewConstant.REVIEW_STATUS_PENDING, message = "请正确输入审核状态")
    @Max(value = QuestionReviewConstant.REVIEW_STATUS_REJECTED, message = "请正确输入审核状态")
    private Integer reviewStatus;
    
    /**
     * 审核意见
     */
    @Size(max = QuestionReviewConstant.REVIEW_MESSAGE_MAX_LENGTH,
        message = "审核意见长度不可超过" + QuestionReviewConstant.REVIEW_MESSAGE_MAX_LENGTH + "个字符")
    private String reviewMessage;
} 

package com.coco8talk.pm.question.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import com.coco8talk.pm.common.dto.BaseQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询审核记录DTO
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryQuestionReviewDTO extends BaseQueryDTO {
    /**
     * 审核记录ID
     */
    @Min(value = 0, message = "请输入正确的审核记录Id")
    private Long id;
    
    /**
     * 题目ID
     */
    @Min(value = 0, message = "请输入正确的题目Id")
    private Long questionId;
    
    /**
     * 审核人ID
     */
    @Min(value = 0, message = "请输入正确的审核人Id")
    private Long reviewerId;
    
    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    @Min(value = QuestionConstant.REVIEW_STATUS_PENDING, message = "请正确输入审核状态")
    @Max(value = QuestionConstant.REVIEW_STATUS_APPROVED, message = "请正确输入审核状态")
    private Integer reviewStatus;
    
    /**
     * 审核开始时间
     */
    private LocalDateTime reviewStartTime;
    
    /**
     * 审核结束时间
     */
    private LocalDateTime reviewEndTime;
} 

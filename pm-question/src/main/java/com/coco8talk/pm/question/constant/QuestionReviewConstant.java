package com.coco8talk.pm.question.constant;

/**
 * 题目审核相关常量
 *
 * @author coco8talk
 * @since 2025/1/20
 */
public class QuestionReviewConstant {
    
    /**
     * 审核意见最大长度
     */
    public static final int REVIEW_MESSAGE_MAX_LENGTH = 500;
    
    /**
     * 审核状态-待审核
     */
    public static final int REVIEW_STATUS_PENDING = 0;
    /**
     * 审核状态-通过
     */
    public static final int REVIEW_STATUS_APPROVED = 1;
    /**
     * 审核状态-拒绝
     */
    public static final int REVIEW_STATUS_REJECTED = 2;
    
    /**
     * 审核状态描述
     */
    public static final String REVIEW_STATUS_PENDING_DESC = "待审核";
    public static final String REVIEW_STATUS_APPROVED_DESC = "通过";
    public static final String REVIEW_STATUS_REJECTED_DESC = "拒绝";
} 

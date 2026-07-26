package com.coco8talk.pm.question.constant;

/**
 * 题目相关常量
 *
 * @author coco8talk
 * @since 2025/1/20
 **/
public class QuestionConstant {
    /**
     * 审核状态-待审核
     */
    public static final int REVIEW_STATUS_PENDING = 0;
    /**
     * 审核状态-通过
     */
    public static final int REVIEW_STATUS_APPROVED = 1;
    
    /**
     * VIP权限-需要VIP
     */
    public static final int NEED_VIP_YES = 1;
    
    // ========== 题目字段长度限制常量 ==========
    
    /**
     * 题目标题最大长度
     */
    public static final int TITLE_MAX_LENGTH = 200;
    
    /**
     * 题目内容最大长度
     */
    public static final int CONTENT_MAX_LENGTH = 500;
    
    /**
     * 题目答案最大长度
     */
    public static final int ANSWER_MAX_LENGTH = 20000;
    
    /**
     * 题目标签最大数量
     */
    public static final int TAGS_MAX_COUNT = 10;
    
    /**
     * 搜索关键词最大长度
     */
    public static final int SEARCH_TEXT_MAX_LENGTH = 50;
    
    // ========== 题目难度常量 ==========
    
    /**
     * 题目难度最小值
     */
    public static final int DIFFICULTY_MIN_VALUE = 0;
    
    /**
     * 题目难度最大值
     */
    public static final int DIFFICULTY_MAX_VALUE = 2;
    
    // ========== 题库相关常量 ==========
    
    /**
     * 题库标题最大长度
     */
    public static final int QUESTION_BANK_TITLE_MAX_LENGTH = 50;
    
    /**
     * 题库描述最大长度
     */
    public static final int QUESTION_BANK_DESCRIPTION_MAX_LENGTH = 200;
    
    /**
     * 题库搜索关键词最大长度
     */
    public static final int QUESTION_BANK_SEARCH_TEXT_MAX_LENGTH = 100;
} 

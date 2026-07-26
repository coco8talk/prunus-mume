package com.coco8talk.pm.question.bank.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import com.coco8talk.pm.common.dto.BaseQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询题库条件封装类
 *
 * @author coco8talk
 * @since 2025/6/27 0:16
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryQuestionBankDTO extends BaseQueryDTO {
    /**
     * 题库ID
     */
    @Min(value = 0, message = "请输入正确的题库Id")
    private Long id;
    
    /**
     * 排除的题库ID
     */
    @Min(value = 0, message = "请输入正确的排除题库Id")
    private Long notId;
    
    /**
     * 题库名称
     */
    @Size(max = QuestionConstant.QUESTION_BANK_TITLE_MAX_LENGTH,
        message = "题库标题不可超过" + QuestionConstant.QUESTION_BANK_TITLE_MAX_LENGTH + "个字符")
    private String title;
    
    /**
     * 创建用户ID
     */
    @Min(value = 0, message = "请输入正确的创建用户Id")
    private Long userId;
    
    /**
     * 题库描述
     */
    @Size(max = QuestionConstant.QUESTION_BANK_DESCRIPTION_MAX_LENGTH,
        message = "题库描述不可超过" + QuestionConstant.QUESTION_BANK_DESCRIPTION_MAX_LENGTH + "个字符")
    private String description;
    
    /**
     * 搜索关键词
     */
    @Size(max = QuestionConstant.QUESTION_BANK_SEARCH_TEXT_MAX_LENGTH,
        message = "题库搜索关键词不可超过" + QuestionConstant.QUESTION_BANK_SEARCH_TEXT_MAX_LENGTH + "个字符")
    private String searchText;
    
    /**
     * 是否需要题目列表（查询时可选）
     */
    private Boolean needQueryQuestionList;
}

package com.coco8talk.pm.question.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import com.coco8talk.pm.common.dto.BaseQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 管理员分页查询题目条件封装类
 *
 * @author coco8talk
 * @since 2025/6/28 16:48
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryQuestionDTO extends BaseQueryDTO {
    /**
     * 题目ID
     */
    @Min(value = 0, message = "请输入合法的题目Id")
    private Long id;
    
    /**
     * 排除的题目Id
     */
    @Min(value = 0, message = "请输入合法的排除题目Id")
    private Long notId;
    
    /**
     * 题库Id
     */
    @Min(value = 0, message = "请输入合法的题目Id")
    private Long questionBankId;
    
    /**
     * 题目名称
     */
    @Size(max = QuestionConstant.TITLE_MAX_LENGTH,
        message = "题目名称不能超过" + QuestionConstant.TITLE_MAX_LENGTH + "个字符")
    private String title;
    
    /**
     * 题目内容
     */
    @Size(max = QuestionConstant.CONTENT_MAX_LENGTH,
        message = "题目内容不能超过" + QuestionConstant.CONTENT_MAX_LENGTH + "个字符")
    private String content;
    
    /**
     * 题目答案
     */
    @Size(max = QuestionConstant.ANSWER_MAX_LENGTH,
        message = "题目答案不能超过" + QuestionConstant.ANSWER_MAX_LENGTH + "个字符")
    private String answer;
    
    /**
     * 创建用户ID
     */
    @Min(value = 0, message = "请输入合法的创建用户Id")
    private Long createUserId;
    
    /**
     * 题目标签
     */
    @Size(max = QuestionConstant.TAGS_MAX_COUNT,
        message = "题目标签不能超过" + QuestionConstant.TAGS_MAX_COUNT + "个")
    private List<String> tags;
    
    /**
     * 题目难度
     */
    @Min(value = QuestionConstant.DIFFICULTY_MIN_VALUE, message = "请输入合法的题目难度")
    @Max(value = QuestionConstant.DIFFICULTY_MAX_VALUE, message = "请输入合法的题目难度")
    private Integer difficulty;
    
    /**
     * 搜索关键词
     */
    @Size(max = QuestionConstant.SEARCH_TEXT_MAX_LENGTH,
        message = "搜索关键词不能超过" + QuestionConstant.SEARCH_TEXT_MAX_LENGTH + "个字符")
    private String searchText;
}

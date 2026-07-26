package com.coco8talk.pm.question.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新建题目信息封装类
 *
 * @author coco8talk
 * @since 2025/6/27 18:19
 **/
@Data
public class CreateQuestionDTO {
    
    /**
     * 题目名称
     */
    @NotBlank(message = "请输入题目名称")
    @Size(max = QuestionConstant.TITLE_MAX_LENGTH,
        message = "题目名称不能超过" + QuestionConstant.TITLE_MAX_LENGTH + "个字符")
    private String title;
    
    /**
     * 题目内容
     */
    @NotBlank(message = "请输入题目内容")
    @Size(max = QuestionConstant.CONTENT_MAX_LENGTH,
        message = "题目内容不能超过" + QuestionConstant.CONTENT_MAX_LENGTH + "个字符")
    private String content;
    
    /**
     * 题目答案
     */
    @NotBlank(message = "请输入题目答案")
    @Size(max = QuestionConstant.ANSWER_MAX_LENGTH,
        message = "题目答案不可超过" + QuestionConstant.ANSWER_MAX_LENGTH + "个字符")
    private String answer;
    
    /**
     * 题目标签
     */
    @Size(max = QuestionConstant.TAGS_MAX_COUNT,
        message = "题目标签不可以超过" + QuestionConstant.TAGS_MAX_COUNT + "个")
    private List<String> tags;
    
    /**
     * 题目难度
     */
    @Min(value = QuestionConstant.DIFFICULTY_MIN_VALUE, message = "请输入合法的题目难度")
    @Max(value = QuestionConstant.DIFFICULTY_MAX_VALUE, message = "请输入合法的题目难度")
    private Integer difficulty;
    
    /**
     * 题库ID
     */
    @Positive(message = "请输入合法的题库Id")
    private Long questionBankId;
}

package com.coco8talk.pm.question.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 编辑题目信息封装类
 *
 * @author coco8talk
 * @since 2025/6/27 19:31
 **/
@Data
public class EditQuestionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7063350097164780157L;
    
    /**
     * 题目ID
     */
    @Positive(message = "请传入合法的题目Id")
    private Long id;
    
    /**
     * 题目标题
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
     * 题目难度
     */
    @Min(value = QuestionConstant.DIFFICULTY_MIN_VALUE, message = "请输入合法的题目难度")
    @Max(value = QuestionConstant.DIFFICULTY_MAX_VALUE, message = "请输入合法的题目难度")
    private Integer difficulty;
    
    /**
     * 题目标签
     */
    @Size(max = QuestionConstant.TAGS_MAX_COUNT,
        message = "题目标签不能超过" + QuestionConstant.TAGS_MAX_COUNT + "个")
    private List<String> tags;
    
}

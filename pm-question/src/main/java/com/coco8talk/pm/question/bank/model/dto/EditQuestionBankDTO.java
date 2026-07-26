package com.coco8talk.pm.question.bank.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑题库信息参数封装类
 *
 * @author coco8talk
 * @since 2025/6/26 23:13
 **/
@Data
public class EditQuestionBankDTO {
    /**
     * 题库ID
     */
    @Positive(message = "请传入合法的题库Id")
    private Long id;
    
    /**
     * 题库名称
     */
    @Size(max = QuestionConstant.QUESTION_BANK_TITLE_MAX_LENGTH,
        message = "题库标题不可超过" + QuestionConstant.QUESTION_BANK_TITLE_MAX_LENGTH + "个字符")
    private String title;
    
    /**
     * 题库图片
     */
    private String picture;
    
    /**
     * 题库描述
     */
    @Size(max = QuestionConstant.QUESTION_BANK_DESCRIPTION_MAX_LENGTH,
        message = "题库描述不可超过" + QuestionConstant.QUESTION_BANK_DESCRIPTION_MAX_LENGTH + "个字符")
    private String description;
}

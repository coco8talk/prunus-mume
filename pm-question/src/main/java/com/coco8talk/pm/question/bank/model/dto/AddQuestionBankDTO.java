package com.coco8talk.pm.question.bank.model.dto;

import com.coco8talk.pm.question.constant.QuestionConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建题库信息封装类
 *
 * @author coco8talk
 * @since 2025/6/26 21:50
 **/
@Data
public class AddQuestionBankDTO {
    /**
     * 题库名称
     */
    @NotBlank(message = "请输入题库名称")
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

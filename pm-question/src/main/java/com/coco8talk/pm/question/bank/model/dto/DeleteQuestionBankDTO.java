package com.coco8talk.pm.question.bank.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 删除题库信息封装类
 *
 * @author coco8talk
 * @since 2025/6/26 22:45
 **/
@Data
public class DeleteQuestionBankDTO {
    /**
     * 题库ID
     */
    @Positive(message = "请传入合法的题库Id")
    private Long id;
}

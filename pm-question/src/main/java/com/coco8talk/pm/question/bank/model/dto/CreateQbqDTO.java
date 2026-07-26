package com.coco8talk.pm.question.bank.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 创建 题库-题目 关联参数封装类
 *
 * @author coco8talk
 * @since 2025/6/28 17:26
 **/
@Data
public class CreateQbqDTO {
    /**
     * 题库ID
     */
    @Positive(message = "请传入合法的题库Id")
    private Long questionBankId;
    
    /**
     * 题目ID
     */
    @Positive(message = "请传入合法的题目Id")
    private Long questionId;
    
    /**
     * 创建用户的Id
     */
    @Positive(message = "请传入合法的用户Id")
    private Long createUserId;
}

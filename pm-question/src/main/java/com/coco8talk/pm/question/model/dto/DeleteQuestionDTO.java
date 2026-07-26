package com.coco8talk.pm.question.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除题目条件封装类
 *
 * @author coco8talk
 * @since 2025/6/27 19:00
 **/
@Data
public class DeleteQuestionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1419089464962987501L;
    
    /**
     * 题目ID
     */
    @Positive(message = "请传入合法的题目Id")
    private Long id;
}

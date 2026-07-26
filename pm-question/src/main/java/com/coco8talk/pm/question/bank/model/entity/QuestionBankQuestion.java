package com.coco8talk.pm.question.bank.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库-题目关联表
 *
 * @author coco8talk
 * @TableName question_bank_question
 */
@TableName(value = "question_bank_question")
@Data
public class QuestionBankQuestion {
    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 题库ID
     */
    private Long questionBankId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 创建用户ID
     */
    private Long createUserId;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
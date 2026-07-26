package com.coco8talk.pm.question.bank.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库表
 *
 * @author coco8talk
 * @TableName question_bank
 */
@TableName(value = "question_bank")
@Data
public class QuestionBank {
    /**
     * 题库ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 题库名称
     */
    private String title;
    
    /**
     * 题库图片
     */
    private String picture;
    
    /**
     * 创建用户ID
     */
    private Long createUserId;
    
    /**
     * 题库描述
     */
    private String description;
    
    /**
     * 题目数量
     */
    private Integer questionCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 编辑时间
     */
    private LocalDateTime editTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除 0-正常 其余为被删行id
     */
    @TableLogic
    private Long deleted;
}
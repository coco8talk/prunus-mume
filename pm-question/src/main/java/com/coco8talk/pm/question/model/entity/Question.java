package com.coco8talk.pm.question.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目表
 *
 * @author coco8talk
 * @TableName question
 */
@TableName(value = "question")
@Data
public class Question implements Serializable {
    @Serial
    private static final long serialVersionUID = 3387221006229131841L;
    
    /**
     * 题目ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 题目名称
     */
    private String title;
    
    /**
     * 题目内容
     */
    private String content;
    
    /**
     * 题目答案
     */
    private String answer;
    
    /**
     * 创建用户ID
     */
    private Long createUserId;
    
    /**
     * 题目标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    
    /**
     * 难度等级 1-简单 2-中等 3-困难
     */
    private Integer difficulty;
    
    /**
     * 浏览量
     */
    private Integer viewNum;
    
    /**
     * 点赞量
     */
    private Integer thumbNum;
    
    /**
     * 收藏数
     */
    private Integer favourNum;
    
    /**
     * 仅会员可见 0-否 1-是
     */
    private Integer needVip;
    
    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    private Integer reviewStatus;
    
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
    
    /**
     * 乐观锁字段
     */
    @Version
    private Integer version;
}
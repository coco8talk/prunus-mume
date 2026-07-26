package com.coco8talk.pm.interaction.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户点赞表
 *
 * @author coco8talk
 * @TableName user_thumb
 */
@TableName(value = "user_thumb")
@Data
public class UserThumb {
    /**
     * 点赞ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 点赞时间
     */
    private LocalDateTime createTime;
}
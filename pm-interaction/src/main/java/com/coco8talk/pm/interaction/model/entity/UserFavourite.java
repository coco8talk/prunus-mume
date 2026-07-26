package com.coco8talk.pm.interaction.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏表
 *
 * @author coco8talk
 * @TableName user_favourite
 */
@TableName(value = "user_favourite")
@Data
public class UserFavourite {
    /**
     * 收藏ID
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
     * 收藏时间
     */
    private LocalDateTime createTime;
}
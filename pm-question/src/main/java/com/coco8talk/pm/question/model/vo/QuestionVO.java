package com.coco8talk.pm.question.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 题目视图对象
 * @author coco8talk
 * @since 2025/6/26 21:41
 **/
@Data
public class QuestionVO {
    
    /**
     * 题目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
}

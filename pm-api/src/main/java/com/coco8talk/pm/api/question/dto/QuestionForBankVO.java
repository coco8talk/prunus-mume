package com.coco8talk.pm.api.question.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 查询题库所需的题目信息封装类
 *
 * @author coco8talk
 * @since 2025/7/6 12:03
 **/
@Data
public class QuestionForBankVO {
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
     * 题目标签
     */
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

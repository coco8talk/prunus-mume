package com.coco8talk.pm.question.bank.model.vo;

import com.coco8talk.pm.api.user.dto.UserView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库管理员视图脱敏信息返回封装类
 *
 * @author coco8talk
 * @since 2025/1/14 15:30
 **/
@Data
public class QuestionBankForAdminVO {
    /**
     * 题库ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 创建用户脱敏信息
     */
    private UserView createUser;
}
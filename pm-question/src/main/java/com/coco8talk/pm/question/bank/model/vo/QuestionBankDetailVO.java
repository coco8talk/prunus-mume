package com.coco8talk.pm.question.bank.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.api.user.dto.UserView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库表脱敏详细信息返回封装类
 *
 * @author coco8talk
 * @since 2025/6/26 21:37
 **/
@Data
public class QuestionBankDetailVO {
    
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
    private Long userId;
    
    /**
     * 题库描述
     */
    private String description;

    /**
     * 题目数量（仅管理员可见）
     */
    private Integer questionCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 编辑时间（仅管理员可见）
     */
    private LocalDateTime editTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建用户信息
     */
    private UserView userVO;
    
    /**
     * 分页题目脱敏信息
     */
    private Page<QuestionForBankVO> questionPageVO;
}

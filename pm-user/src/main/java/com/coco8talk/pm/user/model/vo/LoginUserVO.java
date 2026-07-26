package com.coco8talk.pm.user.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录返回数据封装类
 * @author coco8talk
 * @since 2025/6/23 1:08
 **/
@Data
public class LoginUserVO {
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 用户权限 0-管理员-admin 1-普通用户-user 2-VIP用户 3-封禁用户-ban
     */
    private Integer userRole;
    
    /**
     * 用户简介
     */
    private String userProfile;
    
    /**
     * 手机号
     */
    private String phoneNumber;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 年级（1-小学 2-初中 3-高中 4-大一 5-大二 6-大三 7-大四 8-硕士 9-博士）
     */
    private Integer grade;
    
    /**
     * 工作经验
     */
    private String workExperience;
    
    /**
     * 擅长方向
     */
    private String expertiseDirection;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

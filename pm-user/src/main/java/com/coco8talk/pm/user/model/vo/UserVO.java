package com.coco8talk.pm.user.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户脱敏信息返回封装类
 *
 * @author coco8talk
 * @since 2025/6/24 17:13
 **/
@Data
public class UserVO {
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 用户名称
     */
    private String userName;
    
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
}

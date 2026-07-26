package com.coco8talk.pm.user.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 给管理员返回的用户信息封装类
 * @author coco8talk
 * @since 2025/7/4 15:48
 **/
@Data
public class UserForAdminVO {
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 微信开放平台ID
     */
    private String unionId;
    
    /**
     * 微信公众号ID
     */
    private String mpOpenId;
    
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
     * 会员编号
     */
    private String vipNumber;
    
    /**
     * 会员兑换码
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long vipCode;
    
    /**
     * 会员过期时间
     */
    private LocalDateTime vipExpireTime;
    
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
     * 分享码
     */
    private String shareCode;
    
    /**
     * 邀请用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inviteUserId;
}

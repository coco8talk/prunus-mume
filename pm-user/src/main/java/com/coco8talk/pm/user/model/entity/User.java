package com.coco8talk.pm.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 *
 * @author coco8talk
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 3166385017687038786L;
    
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 账号密码
     */
    private String userPassword;
    
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
     * 逻辑删除 0-正常 其余为被删行id
     */
    @TableLogic
    private Long deleted;
    
    /**
     * 分享码
     */
    private String shareCode;
    
    /**
     * 邀请用户ID
     */
    private Long inviteUserId;
}
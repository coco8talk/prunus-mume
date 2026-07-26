package com.coco8talk.pm.user.common.constant;

/**
 * Uer 常量字段
 *
 * @author coco8talk
 * @since 2025/6/23 17:24
 **/
public class UserConstant {
    
    /**
     * 用户存入 redis 使用的 key 主体
     */
    public static final String USER_SESSION_KEY = "user:session";
    
    /**
     * 用户默认密码
     */
    public static final String USER_DEFAULT_PASSWORD = "Ll123456789@";
    
    // ========== 用户字段长度限制常量 ==========
    
    /**
     * 用户账号最小长度
     */
    public static final int USER_ACCOUNT_MIN_LENGTH = 5;
    
    /**
     * 用户账号最大长度
     */
    public static final int USER_ACCOUNT_MAX_LENGTH = 15;
    
    /**
     * 用户密码最小长度
     */
    public static final int USER_PASSWORD_MIN_LENGTH = 8;
    
    /**
     * 用户密码最大长度
     */
    public static final int USER_PASSWORD_MAX_LENGTH = 20;
    
    /**
     * 用户名最小长度
     */
    public static final int USER_NAME_MIN_LENGTH = 1;
    
    /**
     * 用户名最大长度
     */
    public static final int USER_NAME_MAX_LENGTH = 20;
    
    /**
     * 用户简介最大长度
     */
    public static final int USER_PROFILE_MAX_LENGTH = 150;
    
    /**
     * 用户年级最小值
     */
    public static final int USER_GRADE_MIN_VALUE = 1;
    
    /**
     * 用户年级最大值
     */
    public static final int USER_GRADE_MAX_VALUE = 9;
    
    /**
     * 用户工作经验最大长度
     */
    public static final int USER_WORK_EXPERIENCE_MAX_LENGTH = 150;
    
    /**
     * 用户专业方向最大长度
     */
    public static final int USER_EXPERTISE_DIRECTION_MAX_LENGTH = 150;
    
    // ========== 用户权限相关常量 ==========
    
    /**
     * 用户权限最小值
     */
    public static final int USER_ROLE_MIN_VALUE = 0;
    
    /**
     * 用户权限最大值
     */
    public static final int USER_ROLE_MAX_VALUE = 3;
    
    // ========== 用户账号格式验证正则 ==========
    
    /**
     * 用户账号格式验证正则（只允许字母、数字、下划线）
     */
    public static final String USER_ACCOUNT_FORMAT_REGEX = "^[a-zA-Z0-9_]+$";
    
    /**
     * 用户手机号格式验证正则
     */
    public static final String USER_PHONE_FORMAT_REGEX = "^1[3-9]\\d{9}$";
    
    /**
     * 用户邮箱格式验证正则
     */
    public static final String USER_EMAIL_FORMAT_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    // ========== 用户角色相关常量 ==========
    
    /**
     * 管理员权限常量
     */
    public static final String ADMIN_USER_ROLE = "admin";
    
    /**
     * VIP用户权限常量
     */
    public static final String VIP_USER_ROLE = "vip";
}

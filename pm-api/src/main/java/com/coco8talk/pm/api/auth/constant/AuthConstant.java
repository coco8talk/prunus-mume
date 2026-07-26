package com.coco8talk.pm.api.auth.constant;

/**
 * 认证模块常量字段
 *
 * @author coco8talk
 */
public class AuthConstant {

    /**
     * 用户存入 session 使用的 key 主体
     */
    public static final String USER_SESSION_KEY = "user:session";

    /**
     * 管理员权限常量
     */
    public static final String ADMIN_USER_ROLE = "admin";

    // ========== 登录主体字段校验常量 ==========

    /**
     * 用户账号最小长度
     */
    public static final int USER_ACCOUNT_MIN_LENGTH = 5;

    /**
     * 用户账号最大长度
     */
    public static final int USER_ACCOUNT_MAX_LENGTH = 15;

    /**
     * 用户权限最小值
     */
    public static final int USER_ROLE_MIN_VALUE = 0;

    /**
     * 用户权限最大值
     */
    public static final int USER_ROLE_MAX_VALUE = 3;
}

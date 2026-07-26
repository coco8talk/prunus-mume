package com.coco8talk.pm.api.auth.dto;

import com.coco8talk.pm.api.auth.constant.AuthConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户表
 *
 * @author coco8talk
 * @TableName user
 */
@Data
public class SessionUserDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 3166385017687038786L;
    
    /**
     * 用户ID
     */
    @Min(value = 0, message = "请输入合法的用户Id")
    private Long id;
    
    /**
     * 用户账号
     */
    @Size(min = AuthConstant.USER_ACCOUNT_MIN_LENGTH, max = AuthConstant.USER_ACCOUNT_MAX_LENGTH,
        message = "用户账号长度需在" + AuthConstant.USER_ACCOUNT_MIN_LENGTH + "-" + AuthConstant.USER_ACCOUNT_MAX_LENGTH + "个字符之间")
    private String userAccount;
    
    /**
     * 用户权限 0-管理员-admin 1-普通用户-user 2-VIP用户 3-封禁用户-ban
     */
    @Min(value = AuthConstant.USER_ROLE_MIN_VALUE, message = "请输入合法的用户权限")
    @Max(value = AuthConstant.USER_ROLE_MAX_VALUE, message = "请输入合法的用户权限")
    private Integer userRole;
    
}

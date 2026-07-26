package com.coco8talk.pm.user.model.dto;

import com.coco8talk.pm.user.common.constant.UserConstant;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 管理员添加用户参数封装类
 *
 * @author coco8talk
 * @since 2025/6/25 14:46
 **/
@Data
public class CreateUserDTO {
    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = UserConstant.USER_ACCOUNT_MIN_LENGTH, max = UserConstant.USER_ACCOUNT_MAX_LENGTH,
        message = "用户账号长度需在" + UserConstant.USER_ACCOUNT_MIN_LENGTH + "-" + UserConstant.USER_ACCOUNT_MAX_LENGTH + "个字符之间")
    private String userAccount;
    
    /**
     * 用户名称
     */
    @NotBlank(message = "用户姓名不能为空")
    @Size(min = UserConstant.USER_NAME_MIN_LENGTH, max = UserConstant.USER_NAME_MAX_LENGTH,
        message = "用户名长度应在" + UserConstant.USER_NAME_MIN_LENGTH + "-" + UserConstant.USER_NAME_MAX_LENGTH + "个字符之间")
    private String userName;
    
    /**
     * 用户权限 0-管理员-admin 1-普通用户-user 2-VIP用户 3-封禁用户-ban
     */
    @Min(value = UserConstant.USER_ROLE_MIN_VALUE, message = "请输入合法的用户权限")
    @Max(value = UserConstant.USER_ROLE_MAX_VALUE, message = "请输入合法的用户权限")
    private Integer userRole;
    
    /**
     * 用户头像
     */
    private String userAvatar;
}

package com.coco8talk.pm.user.model.dto;

import com.coco8talk.pm.user.common.constant.UserConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 查询用户参数封装类
 *
 * @author coco8talk
 * @since 2025/6/23 0:59
 **/
@Data
public class RegisterUserDTO {
    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = UserConstant.USER_ACCOUNT_MIN_LENGTH, max = UserConstant.USER_ACCOUNT_MAX_LENGTH,
        message = "用户账号长度需在" + UserConstant.USER_ACCOUNT_MIN_LENGTH + "-" + UserConstant.USER_ACCOUNT_MAX_LENGTH + "个字符之间")
    private String userAccount;
    
    /**
     * 账号密码
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = UserConstant.USER_PASSWORD_MIN_LENGTH, max = UserConstant.USER_PASSWORD_MAX_LENGTH,
        message = "用户密码长度需在" + UserConstant.USER_PASSWORD_MIN_LENGTH + "-" + UserConstant.USER_PASSWORD_MAX_LENGTH + "个字符之间")
    private String userPassword;
    
    /**
     * 检验密码
     */
    @NotBlank(message = "检验密码不能为空")
    @Size(min = UserConstant.USER_PASSWORD_MIN_LENGTH, max = UserConstant.USER_PASSWORD_MAX_LENGTH,
        message = "确认密码长度需在" + UserConstant.USER_PASSWORD_MIN_LENGTH + "-" + UserConstant.USER_PASSWORD_MAX_LENGTH + "个字符之间")
    private String checkPassword;
}

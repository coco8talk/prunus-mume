package com.coco8talk.pm.authserver.dto;

import com.coco8talk.pm.api.auth.constant.AuthConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户登录参数封装类
 *
 * @author coco8talk
 */
@Data
public class LoginUserDTO {
    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = AuthConstant.USER_ACCOUNT_MIN_LENGTH, max = AuthConstant.USER_ACCOUNT_MAX_LENGTH,
        message = "用户账号长度需在" + AuthConstant.USER_ACCOUNT_MIN_LENGTH + "-" + AuthConstant.USER_ACCOUNT_MAX_LENGTH + "个字符之间")
    private String userAccount;

    /**
     * 账号密码
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = AuthConstant.USER_PASSWORD_MIN_LENGTH, max = AuthConstant.USER_PASSWORD_MAX_LENGTH,
        message = "用户密码长度需在" + AuthConstant.USER_PASSWORD_MIN_LENGTH + "-" + AuthConstant.USER_PASSWORD_MAX_LENGTH + "个字符之间")
    private String userPassword;
}

package com.coco8talk.pm.user.model.dto;

import com.coco8talk.pm.user.common.constant.UserConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户编辑自己密码参数封装类
 *
 * @author coco8talk
 * @since 2025/7/4 16:10
 **/
@Data
public class EditOwnPasswordDTO {
    /**
     * 账号密码
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = UserConstant.USER_PASSWORD_MIN_LENGTH, max = UserConstant.USER_PASSWORD_MAX_LENGTH,
        message = "用户密码长度需在8-20个字符之间")
    private String password;
    
    /**
     * 检验密码
     */
    @NotBlank(message = "检验密码不能为空")
    @Size(min = UserConstant.USER_PASSWORD_MIN_LENGTH, max = UserConstant.USER_PASSWORD_MAX_LENGTH,
        message = "确认密码长度需在8-20个字符之间")
    private String checkPassword;
}

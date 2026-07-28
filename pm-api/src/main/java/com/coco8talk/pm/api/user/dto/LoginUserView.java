package com.coco8talk.pm.api.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/** 登录成功后返回的脱敏用户信息，供 auth 模块登录接口直接透出。镜像 pm-user 的 LoginUserVO。 */
public record LoginUserView(
        @JsonSerialize(using = ToStringSerializer.class) Long id,
        String userAccount,
        String userAvatar,
        Integer userRole,
        String userProfile,
        String phoneNumber,
        String email,
        Integer grade,
        String workExperience,
        String expertiseDirection,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}

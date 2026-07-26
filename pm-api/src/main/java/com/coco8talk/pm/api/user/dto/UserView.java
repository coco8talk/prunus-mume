package com.coco8talk.pm.api.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;

/** 跨模块用户展示视图（公开）。镜像 UserVO 的展示字段。 */
public record UserView(
        @JsonSerialize(using = ToStringSerializer.class) Long id,
        String userName,
        String userAvatar,
        Integer userRole,
        String userProfile,
        LocalDateTime createTime) {
}

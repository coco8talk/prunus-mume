package com.coco8talk.pm.user.model.dto;

import com.coco8talk.pm.common.dto.BaseQueryDTO;
import com.coco8talk.pm.user.common.constant.UserConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询用户参数封装类
 *
 * @author coco8talk
 * @since 2025/6/23 20:48
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryUserDTO extends BaseQueryDTO {
    /**
     * 用户ID
     */
    @Min(value = 0, message = "请输入合法的用户Id")
    private Long id;
    
    /**
     * 微信开放平台ID
     */
    @Size(max = 100, message = "用户微信开放平台ID应在1-100个字符之间")
    private String unionId;
    
    /**
     * 微信公众号ID
     */
    @Size(min = 1, max = 20, message = "用户微信公众号ID应在1-100个字符之间")
    private String mpOpenId;
    
    /**
     * 用户名称
     */
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
     * 用户简介
     */
    @Size(max = UserConstant.USER_PROFILE_MAX_LENGTH,
        message = "用户简介长度不可以超过" + UserConstant.USER_PROFILE_MAX_LENGTH + "个字符")
    private String userProfile;
}

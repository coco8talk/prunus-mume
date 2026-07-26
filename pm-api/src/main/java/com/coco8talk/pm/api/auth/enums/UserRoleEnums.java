package com.coco8talk.pm.api.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举类，用于自定义鉴权注解
 * 定义了系统中用户可能拥有的不同角色
 *
 * @author coco8talk
 * @since 2025/6/24 17:58
 **/
@Getter
@AllArgsConstructor
public enum UserRoleEnums {
    
    // 管理员角色，拥有系统最高权限
    ADMIN(0, "admin"),
    // 普通用户角色，具有基本的系统访问权限
    NORMAL(1, "normal"),
    // VIP用户角色，享有特殊服务和优惠
    VIP(2, "vip"),
    // 封禁用户角色，被系统禁止访问
    BAN(3, "ban");
    
    /**
     * 角色字符串表示，与前端的交互方式
     */
    private final Integer code;
    /**
     * 角色描述
     */
    private final String role;
    
    /**
     * 根据code获取对应的role
     * @param code 角色代码
     * @return 对应的role字符串，如果找不到则返回null
     */
    public static String getRoleByCode(Integer code) {
        for (UserRoleEnums userRole : UserRoleEnums.values()) {
            if (userRole.getCode().equals(code)) {
                return userRole.getRole();
            }
        }
        return null;
    }
}


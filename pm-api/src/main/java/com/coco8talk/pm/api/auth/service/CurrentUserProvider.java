package com.coco8talk.pm.api.auth.service;

import com.coco8talk.pm.api.auth.enums.UserRoleEnums;

/** 其它模块获取"当前登录用户"的唯一入口，屏蔽 Sa-Token / session 细节。 */
public interface CurrentUserProvider {
    /** 当前登录用户 id；未登录抛业务异常。 */
    Long currentUserId();

    /** 当前请求是否已登录（不抛异常）。 */
    boolean isLoggedIn();

    /** 当前登录用户角色枚举。 */
    UserRoleEnums currentUserRole();

    /** 是否为管理员。 */
    boolean isAdmin();
}

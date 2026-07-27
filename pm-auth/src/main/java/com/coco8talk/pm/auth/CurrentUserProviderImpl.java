package com.coco8talk.pm.auth;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.auth.enums.UserRoleEnums;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.ThrowUtils;
import org.springframework.stereotype.Component;

/**
 * {@link CurrentUserProvider} 默认实现，委托给 {@link SessionManagerUtil} 读取当前登录用户。
 *
 * @author coco8talk
 */
@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {

    private final SessionManagerUtil sessionManagerUtil;

    public CurrentUserProviderImpl(SessionManagerUtil sessionManagerUtil) {
        this.sessionManagerUtil = sessionManagerUtil;
    }

    @Override
    public Long currentUserId() {
        // getLonginUserInfo 在未登录时抛业务异常
        return sessionManagerUtil.getLonginUserInfo().getId();
    }

    @Override
    public boolean isLoggedIn() {
        return sessionManagerUtil.isUserLoggedIn();
    }

    @Override
    public UserRoleEnums currentUserRole() {
        SessionUserDTO sessionUser = sessionManagerUtil.getLonginUserInfo();
        return resolveRole(sessionUser.getUserRole());
    }

    @Override
    public boolean isAdmin() {
        return currentUserRole() == UserRoleEnums.ADMIN;
    }

    /**
     * 根据角色 code 解析角色枚举，未匹配到时抛业务异常。
     */
    private UserRoleEnums resolveRole(Integer roleCode) {
        for (UserRoleEnums roleEnum : UserRoleEnums.values()) {
            if (roleEnum.getCode().equals(roleCode)) {
                return roleEnum;
            }
        }
        ThrowUtils.throwIfTrue(true, HttpStatusEnum.UNAUTHORIZED, "未知的用户角色");
        return null;
    }
}

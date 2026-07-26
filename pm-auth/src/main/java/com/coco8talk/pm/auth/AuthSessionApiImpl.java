package com.coco8talk.pm.auth;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.auth.service.AuthSessionApi;
import org.springframework.stereotype.Component;

/**
 * {@link AuthSessionApi} 的内部实现，委托给 {@link SessionManagerUtil} 完成 session 读写。
 * 让其它模块（如 user）通过公开接口管理会话，而不直接依赖 auth.internal。
 *
 * @author coco8talk
 */
@Component
public class AuthSessionApiImpl implements AuthSessionApi {

    private final SessionManagerUtil sessionManagerUtil;

    public AuthSessionApiImpl(SessionManagerUtil sessionManagerUtil) {
        this.sessionManagerUtil = sessionManagerUtil;
    }

    @Override
    public void login(SessionUserDTO principal) {
        sessionManagerUtil.setUserSession(principal);
    }

    @Override
    public void logout() {
        sessionManagerUtil.logOut();
    }

    @Override
    public void logoutById(Long userId) {
        sessionManagerUtil.logOutById(userId);
    }

    @Override
    public void updateSession(SessionUserDTO principal) {
        sessionManagerUtil.updateUserSession(principal);
    }

    @Override
    public void refreshSession(Long userId, SessionUserDTO principal) {
        sessionManagerUtil.refreshUserSession(userId, principal);
    }

    @Override
    public boolean isCurrentUser(Long userId) {
        return sessionManagerUtil.isCurrentUser(userId);
    }

    @Override
    public SessionUserDTO currentSession() {
        return sessionManagerUtil.getLonginUserInfo();
    }
}

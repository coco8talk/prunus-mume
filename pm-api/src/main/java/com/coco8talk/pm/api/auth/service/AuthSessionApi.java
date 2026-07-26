package com.coco8talk.pm.api.auth.service;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;

public interface AuthSessionApi {
    void login(SessionUserDTO principal);
    void logout();
    void logoutById(Long userId);
    void updateSession(SessionUserDTO principal);
    /**
     * 按 userId 刷新指定用户的 Sa-Token Session 中的 SessionUserDTO。
     * 用于支付回调等非当前用户登录态的异步场景。
     */
    void refreshSession(Long userId, SessionUserDTO principal);
    boolean isCurrentUser(Long userId);
    SessionUserDTO currentSession();
}

package com.coco8talk.pm.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.BizException;
import com.coco8talk.pm.common.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Session管理工具类
 * 统一管理用户session的创建、获取、删除等操作
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@Slf4j
@Component
public class SessionManagerUtil {
    
    /**
     * 设置用户session信息
     *
     * @param sessionUserDTO 用户session信息
     */
    public void setUserSession(SessionUserDTO sessionUserDTO) {
        StpUtil.getSession().set(AuthConstant.USER_SESSION_KEY, sessionUserDTO);
        log.debug("用户session已设置: userId={}", sessionUserDTO.getId());
    }
    
    /**
     * 获取当前用户session信息（必须登录）
     *
     * @return 用户session信息
     * @throws BizException 如果用户未登录
     */
    public SessionUserDTO getLonginUserInfo() {
        Object sessionUserDTO = StpUtil.getSession().get(AuthConstant.USER_SESSION_KEY);
        ThrowUtils.throwIfNull(sessionUserDTO, HttpStatusEnum.UNAUTHORIZED, "请先登录");
        return (SessionUserDTO) sessionUserDTO;
    }
    
    /**
     * 检查用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public boolean isUserLoggedIn() {
        return StpUtil.isLogin();
    }
    
    /**
     * 检查指定用户是否为当前登录用户
     *
     * @param userId 用户ID
     * @return true-是当前用户，false-不是当前用户
     */
    public boolean isCurrentUser(Long userId) {
        SessionUserDTO currentUser = getLonginUserInfo();
        return currentUser != null && currentUser.getId().equals(userId);
    }
    
    /**
     * 根据用户id清除用户session信息
     *
     * @param userId 用户id
     */
    public void logOutById(Long userId) {
        StpUtil.logout(userId);
    }
    
    /**
     * 使session失效（完全清除session）
     */
    public void logOut() {
        StpUtil.logout();
    }
    
    /**
     * 更新用户session信息
     *
     * @param sessionUserDTO 新的用户session信息
     */
    public void updateUserSession(SessionUserDTO sessionUserDTO) {
        if (isUserLoggedIn()) {
            setUserSession(sessionUserDTO);
            log.debug("用户session已更新: userId={}", sessionUserDTO.getId());
        }
    }

    /**
     * 按 userId 刷新指定用户的 Sa-Token Session（不依赖当前请求登录态）。
     * 用于支付回调等异步场景。
     *
     * @param userId         用户 ID
     * @param sessionUserDTO 新的 session 信息
     */
    public void refreshUserSession(Long userId, SessionUserDTO sessionUserDTO) {
        StpUtil.getSessionByLoginId(userId).set(AuthConstant.USER_SESSION_KEY, sessionUserDTO);
        log.debug("通过 loginId 刷新用户session: userId={}", userId);
    }
}

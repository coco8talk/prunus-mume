package com.coco8talk.pm.authserver.service;

import cn.dev33.satoken.stp.StpUtil;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.auth.service.AuthSessionApi;
import com.coco8talk.pm.api.user.dto.LoginUserView;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.authserver.dto.LoginUserDTO;
import com.coco8talk.pm.authserver.dto.RegisterUserDTO;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.util.ClientUtil;
import org.springframework.stereotype.Service;

/**
 * 账号注册/登录/登出编排。
 * 账号持久化与凭证校验委托给 {@link UserApi}（跨服务时由 {@code RemoteUserApi} 远程调用 pm-user）；
 * 登录态本身（Sa-Token session）在本服务进程内直接维护。
 */
@Service
public class AuthAccountService {

    private final UserApi userApi;
    private final AuthSessionApi authSessionApi;

    public AuthAccountService(UserApi userApi, AuthSessionApi authSessionApi) {
        this.userApi = userApi;
        this.authSessionApi = authSessionApi;
    }

    /**
     * 用户注册
     */
    public Result<Long> register(RegisterUserDTO registerUserDTO) {
        String userPassword = registerUserDTO.getUserPassword();
        String checkPassword = registerUserDTO.getCheckPassword();
        ThrowUtils.throwIfFalse(userPassword.equals(checkPassword), HttpStatusEnum.BAD_REQUEST, "密码不匹配");

        Long userId = userApi.registerAccount(registerUserDTO.getUserAccount(), userPassword);
        return Result.success(HttpStatusEnum.OK, userId);
    }

    /**
     * 用户登录
     */
    public Result<LoginUserView> login(LoginUserDTO loginUserDTO) {
        LoginUserView loginUserView = userApi.verifyCredentials(loginUserDTO.getUserAccount(), loginUserDTO.getUserPassword());

        SessionUserDTO sessionUserDTO = new SessionUserDTO();
        sessionUserDTO.setId(loginUserView.id());
        sessionUserDTO.setUserAccount(loginUserView.userAccount());
        sessionUserDTO.setUserRole(loginUserView.userRole());

        StpUtil.login(loginUserView.id(), ClientUtil.getRequestDevice());
        authSessionApi.login(sessionUserDTO);

        return Result.success(HttpStatusEnum.OK, loginUserView);
    }

    /**
     * 用户退出登录
     */
    public Result<Void> logout() {
        authSessionApi.currentSession();
        authSessionApi.logout();
        return Result.success(HttpStatusEnum.NO_CONTENT);
    }
}

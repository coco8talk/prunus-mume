package com.coco8talk.pm.user.controller;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.user.dto.LoginUserView;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.common.exception.BizException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Period;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/internal/users")
public class InternalUserApiController {

    private final UserApi userApi;

    public InternalUserApiController(UserApi userApi) {
        this.userApi = userApi;
    }

    @PostMapping("/batch")
    public List<UserView> getByIds(@RequestBody Collection<Long> userIds) {
        return userApi.getByIds(userIds);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserView> getById(@PathVariable Long userId) {
        return ResponseEntity.ofNullable(userApi.getById(userId));
    }

    @PostMapping("/{userId}/vip")
    public void grantVip(@PathVariable Long userId, @RequestBody VipRequest request) {
        userApi.grantVip(userId, Period.parse(request.period()));
    }

    @GetMapping("/{userId}/session")
    public ResponseEntity<SessionUserDTO> toSessionDto(@PathVariable Long userId) {
        return ResponseEntity.ofNullable(userApi.toSessionDto(userId));
    }

    @PutMapping("/{userId}/avatar")
    public void updateAvatar(@PathVariable Long userId, @RequestBody AvatarRequest request) {
        userApi.updateAvatar(userId, request.avatarUrl());
    }

    @PostMapping("/register")
    public Long register(@RequestBody RegisterRequest request) {
        return userApi.registerAccount(request.userAccount(), request.userPassword());
    }

    @PostMapping("/verify-credentials")
    public LoginUserView verifyCredentials(@RequestBody VerifyCredentialsRequest request) {
        return userApi.verifyCredentials(request.userAccount(), request.userPassword());
    }

    /**
     * 仅本控制器内生效：把 register/verify-credentials 的业务校验失败（账号已存在、密码错误等）
     * 转成带真实 HTTP 状态码的响应，供 RemoteUserApi 通过状态码 + body 还原成 BizException。
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<String> handleBizException(BizException e) {
        return ResponseEntity.status(e.getCode()).body(e.getMessage());
    }

    private record VipRequest(String period) {
    }

    private record AvatarRequest(String avatarUrl) {
    }

    private record RegisterRequest(String userAccount, String userPassword) {
    }

    private record VerifyCredentialsRequest(String userAccount, String userPassword) {
    }
}

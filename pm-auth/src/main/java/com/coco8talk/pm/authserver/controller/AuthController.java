package com.coco8talk.pm.authserver.controller;

import com.coco8talk.pm.api.user.dto.LoginUserView;
import com.coco8talk.pm.authserver.dto.LoginUserDTO;
import com.coco8talk.pm.authserver.dto.RegisterUserDTO;
import com.coco8talk.pm.authserver.service.AuthAccountService;
import com.coco8talk.pm.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制类
 *
 * @author coco8talk
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "认证相关接口", description = "提供用户注册、登录、登出能力")
public class AuthController {

    private final AuthAccountService authAccountService;

    public AuthController(AuthAccountService authAccountService) {
        this.authAccountService = authAccountService;
    }

    /**
     * 用户注册功能
     *
     * @param registerUserDTO 用户注册信息
     * @return 用户 id
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "校验注册信息并创建新用户账号，返回生成的用户 ID")
    public Result<Long> register(@RequestBody @Valid RegisterUserDTO registerUserDTO) {
        log.info("用户注册请求: {}", registerUserDTO);
        return authAccountService.register(registerUserDTO);
    }

    /**
     * 用户登录功能
     *
     * @param loginUserDTO 用户登录信息
     * @return 脱敏用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验账号密码并建立登录会话，返回当前用户的脱敏信息")
    public Result<LoginUserView> login(@RequestBody @Valid LoginUserDTO loginUserDTO) {
        log.info("用户登录请求: {}", loginUserDTO);
        return authAccountService.login(loginUserDTO);
    }

    /**
     * 用户退出登录态
     *
     * @return 处理结果
     */
    @PostMapping("/logout")
    @Operation(summary = "注销用户", description = "注销当前用户会话并清理登录状态")
    public Result<Void> logout() {
        log.info("用户注销请求");
        return authAccountService.logout();
    }
}

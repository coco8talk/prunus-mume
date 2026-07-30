package com.coco8talk.pm.api.user.service;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.user.client.UserFeignClientConfig;
import com.coco8talk.pm.api.user.dto.LoginUserView;
import com.coco8talk.pm.api.user.dto.UserView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Period;
import java.util.Collection;
import java.util.List;

@FeignClient(name = "pm-user", path = "/api/internal/users", dismiss404 = true, configuration = UserFeignClientConfig.class)
public interface UserApi {
    /** 按 id 批量查用户展示视图；不存在的 id 略过。 */
    @PostMapping("/batch")
    List<UserView> getByIds(@RequestBody Collection<Long> userIds);

    /** 按单个 id 查；不存在返回 null。 */
    @GetMapping("/{userId}")
    UserView getById(@PathVariable("userId") Long userId);

    /** 升级用户为 VIP，并按 period 叠加/续期 vipExpireTime。 */
    default void grantVip(Long userId, Period period) {
        grantVip(userId, new VipRequest(period.toString()));
    }

    @PostMapping("/{userId}/vip")
    void grantVip(@PathVariable("userId") Long userId, @RequestBody VipRequest request);

    /** 按 userId 返回 SessionUserDTO，供其他模块刷新 session 使用。 */
    @GetMapping("/{userId}/session")
    SessionUserDTO toSessionDto(@PathVariable("userId") Long userId);

    default void updateAvatar(Long userId, String avatarUrl) {
        updateAvatar(userId, new AvatarRequest(avatarUrl));
    }

    @PutMapping("/{userId}/avatar")
    void updateAvatar(@PathVariable("userId") Long userId, @RequestBody AvatarRequest request);

    /** 校验账号/密码格式并创建新账号，返回新用户 id；账号已存在等业务错误以 BizException 抛出。 */
    default Long registerAccount(String userAccount, String userPassword) {
        return registerAccount(new RegisterAccountRequest(userAccount, userPassword));
    }

    @PostMapping("/register")
    Long registerAccount(@RequestBody RegisterAccountRequest request);

    /** 校验账号密码是否匹配，成功返回登录所需的脱敏用户信息；账号不存在或密码错误以 BizException 抛出。 */
    default LoginUserView verifyCredentials(String userAccount, String userPassword) {
        return verifyCredentials(new VerifyCredentialsRequest(userAccount, userPassword));
    }

    @PostMapping("/verify-credentials")
    LoginUserView verifyCredentials(@RequestBody VerifyCredentialsRequest request);

    record VipRequest(String period) {
    }

    record AvatarRequest(String avatarUrl) {
    }

    record RegisterAccountRequest(String userAccount, String userPassword) {
    }

    record VerifyCredentialsRequest(String userAccount, String userPassword) {
    }
}

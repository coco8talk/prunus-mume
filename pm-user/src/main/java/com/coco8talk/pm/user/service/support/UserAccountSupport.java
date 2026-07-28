package com.coco8talk.pm.user.service.support;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coco8talk.pm.common.constant.CommonConstant;
import com.coco8talk.pm.common.exception.BizException;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.user.common.constant.UserConstant;
import com.coco8talk.pm.user.mapper.UserMapper;
import com.coco8talk.pm.user.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 账号相关的共享校验/加密/加锁逻辑。
 * 供 {@link com.coco8talk.pm.user.service.impl.UserServiceImpl}（管理员建号、改密）
 * 与 {@link com.coco8talk.pm.user.service.impl.UserApiImpl}（注册、登录校验，供 auth 模块远程调用）共用。
 */
@Component
public class UserAccountSupport {

    private final UserMapper userMapper;
    private final RedissonClient redissonClient;

    public UserAccountSupport(UserMapper userMapper, RedissonClient redissonClient) {
        this.userMapper = userMapper;
        this.redissonClient = redissonClient;
    }

    /**
     * 使用分布式锁执行用户账号相关操作
     */
    public <T> T executeWithUserAccountLock(String userAccount, UserAccountLockCallback<T> callback) {
        RLock lock = redissonClient.getLock(CommonConstant.USER_ACCOUNT_LOCK_PREFIX + userAccount);
        try {
            boolean isLocked = lock.tryLock(CommonConstant.LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
            ThrowUtils.throwIfFalse(isLocked, HttpStatusEnum.TOO_MANY_REQUESTS, "当前请求过多，请稍后再试");

            return callback.execute();
        } catch (InterruptedException e) {
            throw new BizException(HttpStatusEnum.REQUEST_TIMEOUT.getCode(), "线程中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 用户账号锁回调接口
     */
    @FunctionalInterface
    public interface UserAccountLockCallback<T> {
        T execute();
    }

    /**
     * 密码加密
     */
    public String encryptPassword(String password) {
        try {
            return BCrypt.withDefaults().hashToString(CommonConstant.BCRYPT_ROUNDS, password.toCharArray());
        } catch (Exception e) {
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "密码加密失败");
        }
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            BCrypt.Result verifyResult = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
            return verifyResult.verified;
        } catch (Exception e) {
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "密码验证失败");
        }
    }

    /**
     * 验证用户账号是否存在
     */
    public User validateUserAccountExists(String userAccount, boolean isRegister) {
        ThrowUtils.throwIfBlank(userAccount, HttpStatusEnum.BAD_REQUEST, "用户账号不能为空");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        User userFromDb = userMapper.selectOne(wrapper);

        if (isRegister) {
            // 注册时检查账号是否已存在
            ThrowUtils.throwIfNotNull(userFromDb, HttpStatusEnum.BAD_REQUEST, "该用户账号已存在，请更换后重试");
            return null;
        } else {
            // 登录时检查账号是否存在
            ThrowUtils.throwIfNull(userFromDb, HttpStatusEnum.BAD_REQUEST, "用户账号不存在");
            return userFromDb;
        }
    }

    /**
     * 验证用户账号格式
     */
    public void validateUserAccountFormat(String userAccount) {
        ThrowUtils.throwIfFalse(StringUtils.isNotBlank(userAccount) && userAccount.length() >= UserConstant.USER_ACCOUNT_MIN_LENGTH && userAccount.length() <= UserConstant.USER_ACCOUNT_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "用户账号长度必须在" + UserConstant.USER_ACCOUNT_MIN_LENGTH + "-" + UserConstant.USER_ACCOUNT_MAX_LENGTH + "个字符之间");

        // 检查是否包含特殊字符
        Pattern pattern = Pattern.compile(UserConstant.USER_ACCOUNT_FORMAT_REGEX);
        ThrowUtils.throwIfFalse(pattern.matcher(userAccount).matches(),
            HttpStatusEnum.BAD_REQUEST, "用户账号只能包含字母、数字和下划线");
    }

    /**
     * 验证用户密码格式
     */
    public void validateUserPasswordFormat(String userPassword) {
        ThrowUtils.throwIfFalse(StringUtils.isNotBlank(userPassword) && userPassword.length() >= UserConstant.USER_PASSWORD_MIN_LENGTH && userPassword.length() <= UserConstant.USER_PASSWORD_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "用户密码长度必须在" + UserConstant.USER_PASSWORD_MIN_LENGTH + "-" + UserConstant.USER_PASSWORD_MAX_LENGTH + "个字符之间");
    }
}

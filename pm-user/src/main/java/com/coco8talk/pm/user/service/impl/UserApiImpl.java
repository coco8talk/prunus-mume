package com.coco8talk.pm.user.service.impl;

import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.user.dto.LoginUserView;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.user.convert.UserMapstruct;
import com.coco8talk.pm.user.model.entity.User;
import com.coco8talk.pm.user.mapper.UserMapper;
import com.coco8talk.pm.user.service.support.UserAccountSupport;
import com.coco8talk.pm.api.auth.enums.UserRoleEnums;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** {@link UserApi} 的内部实现，直接读取用户持久层。 */
@Component
public class UserApiImpl implements UserApi {

    private final UserMapper userMapper;
    private final UserAccountSupport userAccountSupport;

    public UserApiImpl(UserMapper userMapper, UserAccountSupport userAccountSupport) {
        this.userMapper = userMapper;
        this.userAccountSupport = userAccountSupport;
    }

    @Override
    public List<UserView> getByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .map(UserApiImpl::toView)
                .toList();
    }

    @Override
    public UserView getById(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user == null ? null : toView(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatarById(avatarUrl, String.valueOf(userId));
    }

    @Override
    public void grantVip(Long userId, Period period) {
        if (userId == null || period == null) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(now))
                ? user.getVipExpireTime() : now;
        user.setVipExpireTime(base.plus(period));
        user.setUserRole(UserRoleEnums.VIP.getCode());
        userMapper.updateById(user);
    }

    @Override
    public SessionUserDTO toSessionDto(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user == null ? null : UserMapstruct.INSTANCE.entityToSessionDto(user);
    }

    @Override
    public Long registerAccount(String userAccount, String userPassword) {
        userAccountSupport.validateUserAccountFormat(userAccount);
        userAccountSupport.validateUserPasswordFormat(userPassword);

        return userAccountSupport.executeWithUserAccountLock(userAccount, () -> {
            // 检查账号唯一性
            userAccountSupport.validateUserAccountExists(userAccount, true);

            // 密码加密
            String encryptedPassword = userAccountSupport.encryptPassword(userPassword);

            // 保存用户
            User userToInsert = new User();
            userToInsert.setUserAccount(userAccount);
            userToInsert.setUserPassword(encryptedPassword);
            userToInsert.setUserRole(UserRoleEnums.NORMAL.getCode());
            boolean saveResult = userMapper.insert(userToInsert) > 0;
            ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "数据库写入失败");

            return userToInsert.getId();
        });
    }

    @Override
    public LoginUserView verifyCredentials(String userAccount, String userPassword) {
        userAccountSupport.validateUserAccountFormat(userAccount);
        userAccountSupport.validateUserPasswordFormat(userPassword);

        // 检查用户是否存在
        User userFromDb = userAccountSupport.validateUserAccountExists(userAccount, false);

        // 验证密码
        boolean isPasswordValid = userAccountSupport.verifyPassword(userPassword, userFromDb.getUserPassword());
        ThrowUtils.throwIfFalse(isPasswordValid, HttpStatusEnum.BAD_REQUEST, "账号或密码错误");

        return new LoginUserView(
                userFromDb.getId(),
                userFromDb.getUserAccount(),
                userFromDb.getUserAvatar(),
                userFromDb.getUserRole(),
                userFromDb.getUserProfile(),
                userFromDb.getPhoneNumber(),
                userFromDb.getEmail(),
                userFromDb.getGrade(),
                userFromDb.getWorkExperience(),
                userFromDb.getExpertiseDirection(),
                userFromDb.getCreateTime(),
                userFromDb.getUpdateTime());
    }

    private static UserView toView(User user) {
        return new UserView(
                user.getId(),
                user.getUserName(),
                user.getUserAvatar(),
                user.getUserRole(),
                user.getUserProfile(),
                user.getCreateTime());
    }
}

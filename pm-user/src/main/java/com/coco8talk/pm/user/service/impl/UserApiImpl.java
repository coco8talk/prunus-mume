package com.coco8talk.pm.user.service.impl;

import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.user.convert.UserMapstruct;
import com.coco8talk.pm.user.model.entity.User;
import com.coco8talk.pm.user.mapper.UserMapper;
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

    public UserApiImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
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

package com.coco8talk.pm.api.user.service;

import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.user.dto.UserView;

import java.time.Period;
import java.util.Collection;
import java.util.List;

public interface UserApi {
    /** 按 id 批量查用户展示视图；不存在的 id 略过。 */
    List<UserView> getByIds(Collection<Long> userIds);

    /** 按单个 id 查；不存在返回 null。 */
    UserView getById(Long userId);

    /** 更新指定用户的头像 URL。 */

    /** 升级用户为 VIP，并按 period 叠加/续期 vipExpireTime。 */
    void grantVip(Long userId, Period period);

    /** 按 userId 返回 SessionUserDTO，供其他模块刷新 session 使用。 */
    SessionUserDTO toSessionDto(Long userId);

    void updateAvatar(Long userId, String avatarUrl);
}

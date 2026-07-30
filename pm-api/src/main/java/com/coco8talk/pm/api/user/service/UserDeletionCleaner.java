package com.coco8talk.pm.api.user.service;

/**
 * 用户删除时的同步清理端口（依赖倒置）。user 模块删除用户时回调所有实现，
 * 让下游模块（如 filestorage）清理与该用户相关的数据（如头像文件），
 * 而无需 user 依赖它们。这是同步直调端口，不是领域事件。
 *
 * TODO: 当前跨服务场景下失效。pm-file-storage 的实现类
 * (AvatarServiceImpl) 所在包未被 pm-user 的 scanBasePackages
 * 扫描到，UserServiceImpl 注入的 List<UserDeletionCleaner> 运行时为空，
 * 删除用户不会真正触发头像清理。需要评估 Feign 直调 vs MQ 事件两种方案重新实现。
 */
public interface UserDeletionCleaner {
    void onUserDeleted(Long userId);
}

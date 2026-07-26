package com.coco8talk.pm.api.user.service;

/**
 * 用户删除时的同步清理端口（依赖倒置）。user 模块删除用户时回调所有实现，
 * 让下游模块（如 filestorage）清理与该用户相关的数据（如头像文件），
 * 而无需 user 依赖它们。这是同步直调端口，不是领域事件。
 */
public interface UserDeletionCleaner {
    void onUserDeleted(Long userId);
}

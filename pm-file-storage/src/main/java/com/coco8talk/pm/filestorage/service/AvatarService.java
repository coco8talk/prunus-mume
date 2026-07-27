package com.coco8talk.pm.filestorage.service;

import com.coco8talk.pm.common.result.Result;

import java.util.Map;

/**
 * @author coco8talk
 * @since 2025/10/16 22:05
 **/
public interface AvatarService {
    
    /**
     * 获取头像上传credentials
     *
     * @param filename 文件名
     * @return credentials json 字符串
     */
    Result<Map<String, Object>> getAvatarCredentials(String filename);

    /**
     * 确认头像上传完成，更新用户头像地址
     *
     * @return 头像url
     */
    Result<String> confirmAvatarUpload();
    
    /**
     * 删除头像
     *
     * @param avatarCosKey 头像url(用户id)
     * @return 删除结果
     */
    Result<Void> deleteAvatar(String avatarCosKey);
}

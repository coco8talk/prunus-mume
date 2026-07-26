package com.coco8talk.pm.filestorage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.api.user.service.UserDeletionCleaner;
import com.coco8talk.pm.filestorage.service.AvatarService;
import com.coco8talk.pm.filestorage.service.support.AvatarCredentials;
import com.coco8talk.pm.filestorage.service.support.GetKeyService;
import com.qcloud.cos.COSClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author coco8talk
 * @since 2025/10/16 22:05
 **/
@Service
@Log4j2
public class AvatarServiceImpl implements AvatarService, UserDeletionCleaner {
    
    private final AvatarCredentials avatarCredentials;
    private final UserApi userApi;
    private final GetKeyService getKeyService;
    private final COSClient cosClient;

    @Value("${coco8talk.tencent.cos.bucketName}")
    private String bucketName;
    @Value("${coco8talk.tencent.cos.region}")
    private String region;

    public AvatarServiceImpl(AvatarCredentials avatarCredentials,
                             UserApi userApi,
                             GetKeyService getKeyService,
                             COSClient cosClient) {
        this.avatarCredentials = avatarCredentials;
        this.userApi = userApi;
        this.getKeyService = getKeyService;
        this.cosClient = cosClient;
    }
    
    @Override
    public Result<Map<String, Object>> getAvatarCredentials(String filename) {
        Map<String, Object> credentials = avatarCredentials.getAvatarCredentials(filename);
        return Result.success(HttpStatusEnum.OK, credentials);
    }
    
    @Override
    public Result<String> confirmAvatarUpload() {
        String avatarCosKey = getKeyService.generateAvatarCosKey();
        Long userId = Long.valueOf(avatarCosKey);
        UserView userInfo = userApi.getById(userId);
        ThrowUtils.throwIfNull(userInfo, HttpStatusEnum.NOT_FOUND, "用户不存在");
        String avatarUrl = StrUtil.format(
            "https://{}.cos.{}.myqcloud.com/{}",
            bucketName, region, avatarCosKey
        );

        userApi.updateAvatar(userId, avatarUrl);
        return Result.success(HttpStatusEnum.OK, avatarUrl);
    }
    
    @Override
    public Result<Void> deleteAvatar(String avatarCosKey) {
        
        boolean avatarExist = cosClient.doesObjectExist(bucketName, avatarCosKey);
        if (!avatarExist) {
            log.info("avatar is not exist: {}", avatarCosKey);
        }
        cosClient.deleteObject(bucketName, avatarCosKey);
        return Result.success(HttpStatusEnum.OK);
    }

    @Override
    public void onUserDeleted(Long userId) {
        deleteAvatar(String.valueOf(userId));
    }
}

package com.coco8talk.pm.filestorage.service.impl;

import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.common.BizException;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.filestorage.service.support.AvatarCredentials;
import com.coco8talk.pm.filestorage.service.support.GetKeyService;
import com.qcloud.cos.COSClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceImplTest {

    @Mock
    private AvatarCredentials avatarCredentials;

    @Mock
    private UserApi userApi;

    @Mock
    private GetKeyService getKeyService;

    @Mock
    private COSClient cosClient;

    private AvatarServiceImpl avatarService;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarServiceImpl(
                avatarCredentials,
                userApi,
                getKeyService,
                cosClient);
        ReflectionTestUtils.setField(avatarService, "bucketName", "avatar-bucket");
        ReflectionTestUtils.setField(avatarService, "region", "ap-guangzhou");
    }

    @Test
    void confirmAvatarUploadBuildsCosUrlAndUpdatesExistingUser() {
        UserView user = new UserView(42L, "梅子", null, 1, null, null);
        when(getKeyService.generateAvatarCosKey()).thenReturn("42");
        when(userApi.getById(42L)).thenReturn(user);

        Result<String> result = avatarService.confirmAvatarUpload();

        String expectedUrl =
                "https://avatar-bucket.cos.ap-guangzhou.myqcloud.com/42";
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getData()).isEqualTo(expectedUrl);
        verify(userApi).updateAvatar(42L, expectedUrl);
    }

    @Test
    void confirmAvatarUploadThrowsNotFoundWithoutUpdatingMissingUser() {
        when(getKeyService.generateAvatarCosKey()).thenReturn("42");
        when(userApi.getById(42L)).thenReturn(null);

        assertThatThrownBy(() -> avatarService.confirmAvatarUpload())
                .isInstanceOf(BizException.class)
                .satisfies(exception -> {
                    BizException bizException = (BizException) exception;
                    assertThat(bizException.getCode()).isEqualTo(404);
                    assertThat(bizException.getMessage()).isEqualTo("用户不存在");
                });
        verify(userApi, never()).updateAvatar(anyLong(), anyString());
    }
}

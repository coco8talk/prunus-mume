package com.coco8talk.pm.filestorage.controller;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.filestorage.service.AvatarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AvatarControllerTest {

    @Mock
    private AvatarService avatarService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AvatarController(avatarService)).build();
    }

    @Test
    void getAvatarCredentialsReturnsUploadConfiguration() throws Exception {
        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("bucket", "avatar-bucket");
        credentials.put("region", "ap-guangzhou");
        credentials.put("key", "42");
        credentials.put("expiredTime", 1_800);
        when(avatarService.getAvatarCredentials("avatar.png"))
                .thenReturn(Result.success(HttpStatusEnum.OK, credentials));

        mockMvc.perform(get("/avatar/getAvatarCredentials")
                        .param("filename", "avatar.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.bucket").value("avatar-bucket"))
                .andExpect(jsonPath("$.data.region").value("ap-guangzhou"))
                .andExpect(jsonPath("$.data.key").value("42"))
                .andExpect(jsonPath("$.data.expiredTime").value(1_800));

        verify(avatarService).getAvatarCredentials("avatar.png");
    }

    @Test
    void getAvatarCredentialsRejectsMissingFilenameBeforeCallingService()
            throws Exception {
        mockMvc.perform(get("/avatar/getAvatarCredentials"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(avatarService);
    }
}

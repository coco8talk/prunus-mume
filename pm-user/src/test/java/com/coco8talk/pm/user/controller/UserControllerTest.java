package com.coco8talk.pm.user.controller;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import com.coco8talk.pm.user.model.dto.QueryUserDTO;
import com.coco8talk.pm.user.model.vo.UserVO;
import com.coco8talk.pm.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void queryUserByIdReturnsCallerShapedUserData() throws Exception {
        UserVO user = new UserVO();
        user.setId(9_007_199_254_740_993L);
        user.setUserName("梅子");
        user.setUserRole(1);
        when(userService.queryUserByIdForCaller(9_007_199_254_740_993L))
                .thenReturn(Result.success(HttpStatusEnum.OK, user));

        mockMvc.perform(get("/users/{userId}", "9007199254740993"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value("9007199254740993"))
                .andExpect(jsonPath("$.data.userName").value("梅子"))
                .andExpect(jsonPath("$.data.userRole").value(1));

        verify(userService).queryUserByIdForCaller(9_007_199_254_740_993L);
    }

    @Test
    void queryUserByIdRejectsNonNumericIdBeforeCallingService() throws Exception {
        mockMvc.perform(get("/users/{userId}", "not-a-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户ID格式不正确"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(userService);
    }

    @Test
    void queryUserPageDelegatesToCallerAwareService() throws Exception {
        UserVO user = new UserVO();
        user.setId(1L);
        user.setUserName("梅子");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Object> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        page.setRecords(java.util.List.of(user));
        when(userService.queryUserPageForCaller(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Result.success(HttpStatusEnum.OK, page));

        mockMvc.perform(post("/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"梅\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].userName").value("梅子"));

        ArgumentCaptor<QueryUserDTO> captor = ArgumentCaptor.forClass(QueryUserDTO.class);
        verify(userService).queryUserPageForCaller(captor.capture());
        assertThat(captor.getValue().getUserName()).isEqualTo("梅");
    }

    @Test
    void queryUserPageRejectsEmptyQueryBeforeCallingService() throws Exception {
        mockMvc.perform(post("/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(userService);
    }

    @Test
    void adminCreateUserPostsToUsersRoot() throws Exception {
        when(userService.adminCreateUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Result.success(HttpStatusEnum.OK, 7L));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"mume_test\",\"userName\":\"梅子\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("7"));
    }

    @Test
    void adminDeleteUserTakesIdFromPathVariable() throws Exception {
        when(userService.adminDeleteUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Result.success(HttpStatusEnum.NO_CONTENT));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/users/{userId}", "7"))
                .andExpect(status().isOk());

        ArgumentCaptor<com.coco8talk.pm.user.model.dto.DeleteUserDTO> captor =
                ArgumentCaptor.forClass(com.coco8talk.pm.user.model.dto.DeleteUserDTO.class);
        verify(userService).adminDeleteUser(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(7L);
    }

    @Test
    void adminEditUserTakesIdFromPathVariableAndRejectsEmptyBody() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/users/{userId}", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(userService);
    }

    @Test
    void adminEditUserSetsIdFromPathEvenWhenBodyOmitsIt() throws Exception {
        when(userService.adminEditUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Result.success(HttpStatusEnum.NO_CONTENT));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/users/{userId}", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"新名字\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<com.coco8talk.pm.user.model.dto.AdminEditUserDTO> captor =
                ArgumentCaptor.forClass(com.coco8talk.pm.user.model.dto.AdminEditUserDTO.class);
        verify(userService).adminEditUser(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(7L);
        assertThat(captor.getValue().getUserName()).isEqualTo("新名字");
    }

    @Test
    void putMeRoutesToSelfEditNotAdminEdit() throws Exception {
        when(userService.userEditSelf(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Result.success(HttpStatusEnum.NO_CONTENT));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"梅子\"}"))
                .andExpect(status().isOk());

        verify(userService).userEditSelf(org.mockito.ArgumentMatchers.any());
        verify(userService, org.mockito.Mockito.never()).adminEditUser(org.mockito.ArgumentMatchers.any());
    }
}

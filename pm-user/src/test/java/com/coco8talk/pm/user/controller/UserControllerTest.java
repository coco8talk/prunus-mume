package com.coco8talk.pm.user.controller;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import com.coco8talk.pm.user.model.vo.UserVO;
import com.coco8talk.pm.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void queryUserByIdReturnsPublicUserData() throws Exception {
        UserVO user = new UserVO();
        user.setId(9_007_199_254_740_993L);
        user.setUserName("梅子");
        user.setUserRole(1);
        when(userService.queryUserById(9_007_199_254_740_993L))
                .thenReturn(Result.success(HttpStatusEnum.OK, user));

        mockMvc.perform(get("/user/{userId}", "9007199254740993"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value("9007199254740993"))
                .andExpect(jsonPath("$.data.userName").value("梅子"))
                .andExpect(jsonPath("$.data.userRole").value(1));

        verify(userService).queryUserById(9_007_199_254_740_993L);
    }

    @Test
    void queryUserByIdRejectsNonNumericIdBeforeCallingService() throws Exception {
        mockMvc.perform(get("/user/{userId}", "not-a-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户ID格式不正确"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(userService);
    }
}

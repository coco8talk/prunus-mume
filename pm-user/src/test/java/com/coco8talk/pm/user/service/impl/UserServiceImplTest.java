package com.coco8talk.pm.user.service.impl;

import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.api.auth.service.AuthSessionApi;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.user.model.vo.UserForAdminVO;
import com.coco8talk.pm.user.model.vo.UserVO;
import com.coco8talk.pm.user.service.support.UserAccountSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserAccountSupport userAccountSupport;
    @Mock
    private AuthSessionApi authSessionApi;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private UserServiceImpl userServiceSpy;

    @BeforeEach
    void setUp() {
        userServiceSpy = org.mockito.Mockito.spy(
                new UserServiceImpl(userAccountSupport, authSessionApi, currentUserProvider, List.of()));
    }

    @Test
    void queryUserByIdForCallerReturnsFullVoForLoggedInAdmin() {
        UserForAdminVO adminVo = new UserForAdminVO();
        adminVo.setId(1L);
        adminVo.setUserName("梅子");
        adminVo.setPhoneNumber("13800000000");
        when(currentUserProvider.isLoggedIn()).thenReturn(true);
        when(currentUserProvider.isAdmin()).thenReturn(true);
        org.mockito.Mockito.doReturn(Result.success(com.coco8talk.pm.common.result.http.HttpStatusEnum.OK, adminVo))
                .when(userServiceSpy).adminQueryUserById(1L);

        Result<Object> result = userServiceSpy.queryUserByIdForCaller(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isSameAs(adminVo);
    }

    @Test
    void queryUserByIdForCallerReturnsRedactedVoForNonAdminCaller() {
        UserVO userVo = new UserVO();
        userVo.setId(1L);
        userVo.setUserName("梅子");
        when(currentUserProvider.isLoggedIn()).thenReturn(true);
        when(currentUserProvider.isAdmin()).thenReturn(false);
        org.mockito.Mockito.doReturn(Result.success(com.coco8talk.pm.common.result.http.HttpStatusEnum.OK, userVo))
                .when(userServiceSpy).queryUserById(1L);

        Result<Object> result = userServiceSpy.queryUserByIdForCaller(1L);

        assertThat(result.getData()).isSameAs(userVo);
    }

    @Test
    void queryUserByIdForCallerReturnsRedactedVoForAnonymousCallerWithoutCallingIsAdmin() {
        UserVO userVo = new UserVO();
        userVo.setId(1L);
        when(currentUserProvider.isLoggedIn()).thenReturn(false);
        org.mockito.Mockito.doReturn(Result.success(com.coco8talk.pm.common.result.http.HttpStatusEnum.OK, userVo))
                .when(userServiceSpy).queryUserById(1L);

        Result<Object> result = userServiceSpy.queryUserByIdForCaller(1L);

        assertThat(result.getData()).isSameAs(userVo);
        org.mockito.Mockito.verify(currentUserProvider, org.mockito.Mockito.never()).isAdmin();
    }
}

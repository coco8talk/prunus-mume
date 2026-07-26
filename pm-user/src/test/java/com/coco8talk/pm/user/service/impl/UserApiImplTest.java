package com.coco8talk.pm.user.service.impl;

import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.user.mapper.UserMapper;
import com.coco8talk.pm.user.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApiImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserApiImpl userApi;

    @Test
    void getByIdReturnsMappedUserView() {
        LocalDateTime createTime = LocalDateTime.of(2026, 7, 26, 10, 30);
        User user = new User();
        user.setId(42L);
        user.setUserName("梅子");
        user.setUserAvatar("https://example.test/avatar.png");
        user.setUserRole(2);
        user.setUserProfile("Java 工程师");
        user.setCreateTime(createTime);
        when(userMapper.selectById(42L)).thenReturn(user);

        UserView result = userApi.getById(42L);

        assertThat(result).isEqualTo(new UserView(
                42L,
                "梅子",
                "https://example.test/avatar.png",
                2,
                "Java 工程师",
                createTime));
        verify(userMapper).selectById(42L);
    }

    @Test
    void getByIdReturnsNullWithoutQueryWhenIdIsNull() {
        UserView result = userApi.getById(null);

        assertThat(result).isNull();
        verifyNoInteractions(userMapper);
    }
}

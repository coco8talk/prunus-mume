package com.coco8talk.pm.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.interaction.mapper.UserThumbMapper;
import com.coco8talk.pm.interaction.model.entity.UserThumb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserThumbServiceImplTest {

    @Mock
    private QuestionApi questionApi;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private DistributedLock distributedLock;

    @Mock
    private UserThumbMapper userThumbMapper;

    private UserThumbServiceImpl userThumbService;

    @BeforeEach
    void setUp() {
        userThumbService = new UserThumbServiceImpl(
                questionApi,
                currentUserProvider,
                distributedLock);
        ReflectionTestUtils.setField(userThumbService, "baseMapper", userThumbMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void thumbQuestionCreatesRecordAndIncrementsCountInsideLock() {
        when(questionApi.exists(42L)).thenReturn(true);
        when(questionApi.isApproved(42L)).thenReturn(true);
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(userThumbMapper.selectOne(any(Wrapper.class), eq(true))).thenReturn(null);
        when(userThumbMapper.insert(any(UserThumb.class))).thenReturn(1);
        when(distributedLock.runExclusive(
                eq("lock:thumb:7:42"),
                eq(0L),
                eq(5_000L),
                any(Supplier.class)))
                .thenAnswer(invocation ->
                        ((Supplier<Result<Boolean>>) invocation.getArgument(3)).get());

        Result<Boolean> result = userThumbService.thumbQuestion(42L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isTrue();
        assertThat(result.getMessage()).isEqualTo("操作成功");

        ArgumentCaptor<UserThumb> recordCaptor = ArgumentCaptor.forClass(UserThumb.class);
        verify(userThumbMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getUserId()).isEqualTo(7L);
        assertThat(recordCaptor.getValue().getQuestionId()).isEqualTo(42L);
        assertThat(recordCaptor.getValue().getCreateTime()).isNotNull();
        verify(questionApi).incrementThumbCount(42L, 1);
        verify(distributedLock).runExclusive(
                eq("lock:thumb:7:42"),
                eq(0L),
                eq(5_000L),
                any(Supplier.class));
    }

    @Test
    void thumbQuestionReturnsFailureWithoutLockWhenQuestionIsNotApproved() {
        when(questionApi.exists(42L)).thenReturn(true);
        when(questionApi.isApproved(42L)).thenReturn(false);

        Result<Boolean> result = userThumbService.thumbQuestion(42L);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).isEqualTo("该题目未通过审核");
        verifyNoInteractions(currentUserProvider, distributedLock, userThumbMapper);
        verify(questionApi, never()).incrementThumbCount(any(), anyInt());
    }
}

package com.coco8talk.pm.question.service.impl;

import com.coco8talk.pm.question.mapper.QuestionMapper;
import com.coco8talk.pm.question.model.entity.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionApiImplTest {

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionApiImpl questionApi;

    @Test
    void existsReturnsTrueWhenQuestionIsFound() {
        Question question = new Question();
        question.setId(42L);
        when(questionMapper.selectById(42L)).thenReturn(question);

        boolean result = questionApi.exists(42L);

        assertThat(result).isTrue();
        verify(questionMapper).selectById(42L);
    }

    @Test
    void existsReturnsFalseWithoutQueryWhenIdIsNull() {
        boolean result = questionApi.exists(null);

        assertThat(result).isFalse();
        verifyNoInteractions(questionMapper);
    }
}

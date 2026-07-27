package com.coco8talk.pm.interaction.controller;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.interaction.service.UserThumbService;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class UserThumbControllerTest {

    @Mock
    private UserThumbService userThumbService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new UserThumbController(userThumbService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void thumbQuestionParsesIdAndReturnsServiceResult() throws Exception {
        long questionId = 9_007_199_254_740_993L;
        when(userThumbService.thumbQuestion(questionId))
                .thenReturn(Result.success(HttpStatusEnum.OK, true));

        mockMvc.perform(post("/thumbs/questions/{questionId}", "9007199254740993"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value(true));

        verify(userThumbService).thumbQuestion(questionId);
    }

    @Test
    void thumbQuestionRejectsNonNumericIdBeforeCallingService() throws Exception {
        mockMvc.perform(post("/thumbs/questions/{questionId}", "not-a-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("题目ID格式不正确"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(userThumbService);
    }
}

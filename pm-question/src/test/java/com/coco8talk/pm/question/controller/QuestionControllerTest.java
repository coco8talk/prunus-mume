package com.coco8talk.pm.question.controller;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import com.coco8talk.pm.question.model.vo.QuestionVO;
import com.coco8talk.pm.question.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new QuestionController(questionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void queryQuestionByIdReturnsQuestionData() throws Exception {
        long questionId = 9_007_199_254_740_993L;
        QuestionVO question = new QuestionVO();
        question.setId(questionId);
        question.setTitle("Java 中的虚拟线程是什么？");
        question.setTags(List.of("Java", "并发"));
        question.setDifficulty(2);
        when(questionService.queryQuestionByIdForCaller(questionId))
                .thenReturn(Result.success(HttpStatusEnum.OK, question));

        mockMvc.perform(get("/questions/{id}", "9007199254740993"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value("9007199254740993"))
                .andExpect(jsonPath("$.data.title").value("Java 中的虚拟线程是什么？"))
                .andExpect(jsonPath("$.data.tags[0]").value("Java"))
                .andExpect(jsonPath("$.data.tags[1]").value("并发"))
                .andExpect(jsonPath("$.data.difficulty").value(2));

        verify(questionService).queryQuestionByIdForCaller(questionId);
    }

    @Test
    void queryQuestionByIdRejectsNonNumericIdBeforeCallingService() throws Exception {
        mockMvc.perform(get("/questions/{id}", "not-a-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("题目ID格式不正确"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(questionService);
    }

    @Test
    void createQuestionMapsBeanValidationFailureToBadRequestResult() throws Exception {
        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求参数错误"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(questionService);
    }

    @Test
    void missingStaticResourceMapsToNotFoundResult() throws Exception {
        MockMvc resourceMockMvc = standaloneSetup(new MissingResourceController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        resourceMockMvc.perform(get("/missing-resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("请求的资源不存在"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @RestController
    static class MissingResourceController {

        @GetMapping("/missing-resource")
        void missingResource() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "missing-resource");
        }
    }
}

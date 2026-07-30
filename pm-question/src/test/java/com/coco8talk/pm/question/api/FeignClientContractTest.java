package com.coco8talk.pm.question.api;

import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.api.user.service.UserApi;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import static org.assertj.core.api.Assertions.assertThat;

class FeignClientContractTest {

    @Test
    void userClientPathIncludesSharedServletContextPath() {
        FeignClient feignClient = UserApi.class.getAnnotation(FeignClient.class);

        assertThat(feignClient.path()).isEqualTo("/api/internal/users");
    }

    @Test
    void questionClientPathIncludesSharedServletContextPath() {
        FeignClient feignClient = QuestionApi.class.getAnnotation(FeignClient.class);

        assertThat(feignClient.path()).isEqualTo("/api/internal/questions");
    }
}

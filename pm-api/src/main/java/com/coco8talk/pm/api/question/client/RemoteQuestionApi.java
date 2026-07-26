package com.coco8talk.pm.api.question.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.api.question.service.QuestionApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class RemoteQuestionApi implements QuestionApi {

    private final RestClient restClient;

    public RemoteQuestionApi(RestClient.Builder builder,
                             @Value("${pm.remote.question-base-url:http://localhost:8083/api}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl + "/internal/questions").build();
    }

    @Override
    public boolean exists(Long questionId) {
        return Boolean.TRUE.equals(restClient.get()
                .uri("/{questionId}/exists", questionId)
                .retrieve()
                .body(Boolean.class));
    }

    @Override
    public boolean isApproved(Long questionId) {
        return Boolean.TRUE.equals(restClient.get()
                .uri("/{questionId}/approved", questionId)
                .retrieve()
                .body(Boolean.class));
    }

    @Override
    public void incrementThumbCount(Long questionId, int delta) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/{questionId}/thumb-count")
                        .queryParam("delta", delta)
                        .build(questionId))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void incrementFavourCount(Long questionId, int delta) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/{questionId}/favour-count")
                        .queryParam("delta", delta)
                        .build(questionId))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public int getThumbCount(Long questionId) {
        Integer count = restClient.get()
                .uri("/{questionId}/thumb-count", questionId)
                .retrieve()
                .body(Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public int getFavourCount(Long questionId) {
        Integer count = restClient.get()
                .uri("/{questionId}/favour-count", questionId)
                .retrieve()
                .body(Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public Page<QuestionForBankVO> queryApprovedForBank(List<Long> questionIds,
                                                        Integer current,
                                                        Integer pageSize,
                                                        Long total) {
        return restClient.post()
                .uri("/approved")
                .body(new ApprovedQuestionsRequest(questionIds, current, pageSize, total))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private record ApprovedQuestionsRequest(List<Long> questionIds,
                                            Integer current,
                                            Integer pageSize,
                                            Long total) {
    }
}

package com.coco8talk.pm.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.api.question.service.QuestionApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/questions")
public class InternalQuestionApiController {

    private final QuestionApi questionApi;

    public InternalQuestionApiController(QuestionApi questionApi) {
        this.questionApi = questionApi;
    }

    @GetMapping("/{questionId}/exists")
    public boolean exists(@PathVariable Long questionId) {
        return questionApi.exists(questionId);
    }

    @GetMapping("/{questionId}/approved")
    public boolean isApproved(@PathVariable Long questionId) {
        return questionApi.isApproved(questionId);
    }

    @PostMapping("/{questionId}/thumb-count")
    public void incrementThumbCount(@PathVariable Long questionId, @RequestParam int delta) {
        questionApi.incrementThumbCount(questionId, delta);
    }

    @PostMapping("/{questionId}/favour-count")
    public void incrementFavourCount(@PathVariable Long questionId, @RequestParam int delta) {
        questionApi.incrementFavourCount(questionId, delta);
    }

    @GetMapping("/{questionId}/thumb-count")
    public int getThumbCount(@PathVariable Long questionId) {
        return questionApi.getThumbCount(questionId);
    }

    @GetMapping("/{questionId}/favour-count")
    public int getFavourCount(@PathVariable Long questionId) {
        return questionApi.getFavourCount(questionId);
    }

    @PostMapping("/approved")
    public Page<QuestionForBankVO> queryApprovedForBank(@RequestBody ApprovedQuestionsRequest request) {
        return questionApi.queryApprovedForBank(
                request.questionIds(),
                request.current(),
                request.pageSize(),
                request.total());
    }

    private record ApprovedQuestionsRequest(List<Long> questionIds,
                                            Integer current,
                                            Integer pageSize,
                                            Long total) {
    }
}

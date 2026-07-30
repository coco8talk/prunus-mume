package com.coco8talk.pm.api.question.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 题目模块对外公开 API。
 * 其他模块只能依赖此接口与公开 VO，不能直接访问 question 模块的内部实现类。
 *
 * @author coco8talk
 */
@FeignClient(name = "pm-question", path = "/api/internal/questions")
public interface QuestionApi {

    /**
     * 判断题目是否存在。
     *
     * @param questionId 题目ID
     * @return 存在返回 true，否则 false
     */
    @GetMapping("/{questionId}/exists")
    boolean exists(@PathVariable("questionId") Long questionId);

    /**
     * 判断题目是否存在且已审核通过。
     * 语义：题目存在 且 reviewStatus == APPROVED。
     * 题目不存在（含 questionId 为 null）一律返回 false。
     *
     * @param questionId 题目ID
     * @return 已审核通过返回 true，否则 false
     */
    @GetMapping("/{questionId}/approved")
    boolean isApproved(@PathVariable("questionId") Long questionId);

    /**
     * 调整题目点赞数（delta 可正可负）。
     *
     * @param questionId 题目ID
     * @param delta      增量
     */
    @PostMapping("/{questionId}/thumb-count")
    void incrementThumbCount(@PathVariable("questionId") Long questionId, @RequestParam("delta") int delta);

    /** 收藏数增量（delta 可为负）；下限 0。 */
    @PostMapping("/{questionId}/favour-count")
    void incrementFavourCount(@PathVariable("questionId") Long questionId, @RequestParam("delta") int delta);

    /** 当前点赞数（题目不存在返回 0）。 */
    @GetMapping("/{questionId}/thumb-count")
    int getThumbCount(@PathVariable("questionId") Long questionId);

    /** 当前收藏数（题目不存在返回 0）。 */
    @GetMapping("/{questionId}/favour-count")
    int getFavourCount(@PathVariable("questionId") Long questionId);

    /**
     * 根据题目ID列表分页查询已审核通过的题目（题库视图）。
     *
     * @param questionIds 题目ID列表
     * @param current     当前页码
     * @param pageSize    每页大小
     * @param total       总记录数（用于构建分页对象）
     * @return 已审核通过的题目分页信息
     */
    default Page<QuestionForBankVO> queryApprovedForBank(List<Long> questionIds,
                                                          Integer current,
                                                          Integer pageSize,
                                                          Long total) {
        return queryApprovedForBank(new ApprovedQuestionsRequest(questionIds, current, pageSize, total));
    }

    @PostMapping("/approved")
    Page<QuestionForBankVO> queryApprovedForBank(@RequestBody ApprovedQuestionsRequest request);

    record ApprovedQuestionsRequest(List<Long> questionIds,
                                    Integer current,
                                    Integer pageSize,
                                    Long total) {
    }
}

package com.coco8talk.pm.question.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.question.model.dto.QueryQuestionReviewDTO;
import com.coco8talk.pm.question.model.dto.ReviewQuestionDTO;
import com.coco8talk.pm.question.model.entity.QuestionReview;
import com.coco8talk.pm.question.model.vo.QuestionReviewVO;
import com.coco8talk.pm.question.model.vo.QuestionReviewWithDetailVO;

/**
 * 题目审核service层
 *
 * @author coco8talk
 * @description 针对表【question_review(题目审核表)】的数据库操作Service
 * @createDate 2025-07-05 19:58:33
 */
public interface QuestionReviewService extends IService<QuestionReview> {
    
    /**
     * 审核题目（管理员）
     *
     * @param reviewQuestionDTO 审核信息
     * @return 审核结果
     */
    Result<Boolean> reviewQuestion(ReviewQuestionDTO reviewQuestionDTO);
    
    /**
     * 根据题目ID查询审核记录
     *
     * @param questionId 题目ID
     * @return 审核记录
     */
    Result<QuestionReviewVO> getReviewByQuestionId(Long questionId);
    
    /**
     * 分页查询审核记录（管理员）
     *
     * @param queryQuestionReviewDTO 查询条件
     * @return 审核记录分页
     */
    Result<Page<QuestionReviewVO>> adminQueryReviewPage(QueryQuestionReviewDTO queryQuestionReviewDTO);
    
    /**
     * 获取待审核题目数量
     *
     * @return 待审核题目数量
     */
    Result<Long> getPendingReviewCount();
    
    /**
     * 获取待审核题目列表（包含题目详细信息）
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 待审核题目分页列表
     */
    Result<Page<QuestionReviewWithDetailVO>> getPendingReviewList(Integer current, Integer pageSize);
}

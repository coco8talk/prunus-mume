package com.coco8talk.pm.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.question.constant.QuestionConstant;
import com.coco8talk.pm.question.convert.QuestionMapStruct;
import com.coco8talk.pm.question.model.entity.Question;
import com.coco8talk.pm.question.mapper.QuestionMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link QuestionApi} 的内部实现。
 * 合并了原 CommonService 的题目查询/审核逻辑，并新增题目点赞数维护。
 *
 * @author coco8talk
 */
@Component
public class QuestionApiImpl implements QuestionApi {

    private final QuestionMapper questionMapper;

    public QuestionApiImpl(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Override
    public boolean exists(Long questionId) {
        if (questionId == null) {
            return false;
        }
        return questionMapper.selectById(questionId) != null;
    }

    @Override
    public boolean isApproved(Long questionId) {
        if (questionId == null) {
            return false;
        }
        // 题目存在 且 reviewStatus == APPROVED
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getId, questionId)
               .eq(Question::getReviewStatus, QuestionConstant.REVIEW_STATUS_APPROVED);
        return questionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void incrementThumbCount(Long questionId, int delta) {
        if (questionId == null) {
            return;
        }
        questionMapper.incrementThumbNum(questionId, delta);
    }

    @Override
    public void incrementFavourCount(Long questionId, int delta) {
        if (questionId == null) {
            return;
        }
        questionMapper.incrementFavourNum(questionId, delta);
    }

    @Override
    public int getThumbCount(Long questionId) {
        if (questionId == null) {
            return 0;
        }
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getThumbNum() == null) {
            return 0;
        }
        return question.getThumbNum();
    }

    @Override
    public int getFavourCount(Long questionId) {
        if (questionId == null) {
            return 0;
        }
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getFavourNum() == null) {
            return 0;
        }
        return question.getFavourNum();
    }

    @Override
    public Page<QuestionForBankVO> queryApprovedForBank(List<Long> questionIds,
                                                        Integer current,
                                                        Integer pageSize,
                                                        Long total) {
        // 处理空结果
        if (questionIds.isEmpty()) {
            return new Page<>(current, pageSize, 0);
        }

        // 1. 直接查询已审核通过的题目
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Question::getId, questionIds)
               .eq(Question::getReviewStatus, QuestionConstant.REVIEW_STATUS_APPROVED);
        List<Question> questions = questionMapper.selectList(wrapper);

        // 如果没有已审核通过的题目，返回空结果
        if (questions.isEmpty()) {
            return new Page<>(current, pageSize, 0);
        }

        // 3. 转换为QuestionForBankVO
        List<QuestionForBankVO> questionForBankVoList = QuestionMapStruct.INSTANCE.entityListToForBankVoList(questions);

        // 4. 构建返回结果（注意：total需要重新计算，因为过滤了未审核的题目）
        Page<QuestionForBankVO> questionForBankVoPage = new Page<>(current, pageSize, questions.size());
        questionForBankVoPage.setRecords(questionForBankVoList);
        return questionForBankVoPage;
    }
}

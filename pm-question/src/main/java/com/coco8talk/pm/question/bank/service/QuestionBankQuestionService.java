package com.coco8talk.pm.question.bank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.question.bank.model.dto.CreateQbqDTO;
import com.coco8talk.pm.question.bank.model.entity.QuestionBankQuestion;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 题目-题库服务层
 *
 * @author coco8talk
 * @description 针对表【question_bank_question(题库-题目关联表)】的数据库操作Service
 * @createDate 2025-06-22 23:21:17
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {
    
    /**
     * 将 题目-题库关联信息写入关联表
     *
     * @param createQbqDTO 关联信息封装类
     */
    void adminCreateQbq(CreateQbqDTO createQbqDTO);
    
    /**
     * 根据 题目Id 删除 题目-题库关联信息
     *
     * @param id 题目Id
     */
    void deleteQbqByQuestionId(@Positive(message = "请传入合法的题目Id") Long id);
    
    /**
     * 根据题库ID获取题目列表（用于题库查询）
     *
     * @param questionBankId 题库ID
     * @param current        当前页
     * @param pageSize       页面大小
     * @return 题目列表（QuestionForBankVO格式）
     */
    Result<Page<QuestionForBankVO>> getQuestionsForBankById(Long questionBankId, Integer current, Integer pageSize);
    
    /**
     * 批量添加题目到题库
     *
     * @param questionBankId 题库ID
     * @param questionIds    题目ID列表
     * @return 添加结果
     */
    Result<Boolean> adminBatchAddQuestionsToBank(Long questionBankId, List<Long> questionIds);
    
    /**
     * 批量从题库移除题目
     *
     * @param questionBankId 题库ID
     * @param questionIds    题目ID列表
     * @return 移除结果
     */
    Result<Boolean> adminBatchRemoveQuestionsFromBank(Long questionBankId, List<Long> questionIds);
}

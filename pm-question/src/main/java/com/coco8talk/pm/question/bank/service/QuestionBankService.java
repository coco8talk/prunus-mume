package com.coco8talk.pm.question.bank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.question.bank.model.dto.AddQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.DeleteQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.EditQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.QueryQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.entity.QuestionBank;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankDetailVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankForAdminVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankVO;
import jakarta.validation.Valid;

/**
 * 题库service层
 *
 * @author coco8talk
 * @description 针对表【question_bank(题库表)】的数据库操作Service
 * @createDate 2025-06-22 23:21:13
 */
public interface QuestionBankService extends IService<QuestionBank> {
    
    /**
     * 管理员新建题库
     *
     * @param addQuestionBankDTO 新建的题库信息
     * @return 新建题库的Id
     */
    Result<Long> adminCreateQuestionBank(@Valid AddQuestionBankDTO addQuestionBankDTO);
    
    /**
     * 管理员删除题库
     *
     * @param deleteQuestionDTO 删除题库的条件
     * @return 删除结果，成功-true
     */
    Result<Boolean> adminDeleteQuestionBank(@Valid DeleteQuestionBankDTO deleteQuestionDTO);
    
    /**
     * 管理员编辑题库（管理员）
     *
     * @param editQuestionBankDTO 需要编辑的信息
     * @return 编辑结果 成功-true
     */
    Result<Boolean> adminEditQuestionBank(@Valid EditQuestionBankDTO editQuestionBankDTO);
    
    /**
     * 根据Id查询题库（支持查询题目列表）
     *
     * @param questionBankId        题库ID
     * @param needQueryQuestionList 是否需要查询题目列表
     * @return 查询结果，题库脱敏信息
     */
    Result<QuestionBankDetailVO> queryQuestionBankById(Long questionBankId, Boolean needQueryQuestionList);

    /** 管理员详情包含管理字段，其他调用者返回公开字段。 */
    Result<QuestionBankDetailVO> queryQuestionBankByIdForCaller(Long questionBankId, Boolean needQueryQuestionList);
    
    /**
     * 分页查询题库（管理员）
     *
     * @param queryQuestionBankDTO 查询条件
     * @return 分页查询结果
     */
    Result<Page<QuestionBankForAdminVO>> adminQueryPageQuestionBank(@Valid QueryQuestionBankDTO queryQuestionBankDTO);
    
    /**
     * 管理员 分页查询 题库（用户）
     *
     * @param queryQuestionBankDTO 查询条件
     * @return 分页查询结果
     */
    Result<Page<QuestionBankVO>> queryPageQuestionBank(@Valid QueryQuestionBankDTO queryQuestionBankDTO);

    /** 管理员返回管理视图，其他调用者返回公开视图。 */
    Result<Object> queryPageQuestionBankForCaller(@Valid QueryQuestionBankDTO queryQuestionBankDTO);
}

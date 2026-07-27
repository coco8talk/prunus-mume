package com.coco8talk.pm.question.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.question.model.dto.CreateQuestionDTO;
import com.coco8talk.pm.question.model.dto.DeleteQuestionDTO;
import com.coco8talk.pm.question.model.dto.EditQuestionDTO;
import com.coco8talk.pm.question.model.dto.QueryQuestionDTO;
import com.coco8talk.pm.question.model.entity.Question;
import com.coco8talk.pm.question.model.vo.QuestionForAdminVO;
import com.coco8talk.pm.question.model.vo.QuestionVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 题目service层
 *
 * @author coco8talk
 * @description 针对表【question(题目表)】的数据库操作Service
 * @createDate 2025-06-22 23:21:09
 */
public interface QuestionService extends IService<Question> {
    
    /**
     * 新建题目（管理员）
     *
     * @param createQuestionDTO 新建的题目信息
     * @return 新建题目的Id
     */
    Result<Long> adminCreateQuestion(CreateQuestionDTO createQuestionDTO);
    
    /**
     * 删除题目（管理员）
     *
     * @param deleteQuestionDTO 题目Id封装类
     * @return 删除结果
     */
    Result<Void> adminDeleteQuestion(DeleteQuestionDTO deleteQuestionDTO);
    
    /**
     * 管理员批量删除题目
     *
     * @param questionIds 题目Id列表
     * @return 删除结果
     */
    Result<Void> adminBatchDeleteQuestion(List<Long> questionIds);
    
    /**
     * 编辑题目信息（管理员）
     *
     * @param editQuestionDTO 编辑信息
     * @return 编辑结果
     */
    Result<Boolean> adminEditQuestion(@Valid EditQuestionDTO editQuestionDTO);
    
    /**
     * 分页查询题目（管理员）
     *
     * @param queryQuestionDTO 查询条件
     * @return 查询结果
     */
    Result<Page<QuestionForAdminVO>> adminQueryQuestionPage(QueryQuestionDTO queryQuestionDTO);
    
    /**
     * 用户创建题目
     *
     * @param createQuestionDTO 创建题目信息
     * @return 创建结果
     */
    Result<Long> createQuestion(CreateQuestionDTO createQuestionDTO);
    
    /**
     * 用户删除自己的题目
     *
     * @param deleteQuestionDTO 删除信息
     * @return 删除结果
     */
    Result<Void> deleteQuestion(DeleteQuestionDTO deleteQuestionDTO);
    
    /**
     * 用户编辑自己的题目
     *
     * @param editQuestionDTO 编辑信息
     * @return 编辑结果
     */
    Result<Boolean> editQuestion(EditQuestionDTO editQuestionDTO);
    
    /**
     * 分页查询题目（普通用户）
     *
     * @param queryQuestionDTO 查询条件封装类
     * @return 查询结果
     */
    Result<Page<QuestionVO>> queryQuestionPage(QueryQuestionDTO queryQuestionDTO);
    
    /**
     * 根据Id查询题目
     *
     * @param id 题目Id
     * @return 脱敏的题目信息
     */
    Result<QuestionVO> queryQuestionById(Long id);
}

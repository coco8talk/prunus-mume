package com.coco8talk.pm.question.bank.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.question.bank.service.QuestionBankQuestionService;
import com.coco8talk.pm.common.util.IdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库题目关联控制类
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@RestController
@RequestMapping("/question-bank-relations")
@Tag(name = "题库题目关联相关接口", description = "提供题库内题目分页查询及管理员批量维护题库题目关系的能力")
@Slf4j
public class QuestionBankQuestionController {
    
    private final QuestionBankQuestionService questionBankQuestionService;
    
    public QuestionBankQuestionController(QuestionBankQuestionService questionBankQuestionService) {
        this.questionBankQuestionService = questionBankQuestionService;
    }
    
        /**
     * 根据题库ID获取题目列表
     *
     * @param questionBankIdStr 题库ID（字符串形式，避免大数精度丢失）
     * @param current        当前页
     * @param pageSize       页面大小
     * @return 题目列表
     */
    @GetMapping("/banks/{questionBankId}/questions")
    @Operation(summary = "获取题库中的题目列表", description = "按题库标识分页查询其中已审核通过的题目")
    public Result<Page<QuestionForBankVO>> getQuestionsByBankId(
        @PathVariable("questionBankId") String questionBankIdStr,
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        Long questionBankId = IdUtils.parseId(questionBankIdStr, "题库ID");
        log.info("获取题库ID为 {} 的题目列表，当前页：{}，页面大小：{}", questionBankId, current, pageSize);
        return questionBankQuestionService.getQuestionsForBankById(questionBankId, current, pageSize);
    }

    /**
     * 批量添加题目到题库
     *
     * @param questionBankIdStr 题库ID（字符串形式，避免大数精度丢失）
     * @param questionIds    题目ID列表
     * @return 添加结果
     */
    @PostMapping("/banks/{questionBankId}/questions/batch")
    @Operation(summary = "批量添加题目到题库", description = "由管理员将一组有效且尚未关联的题目批量加入指定题库")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Boolean> adminBatchAddQuestionsToBank(
        @PathVariable("questionBankId") String questionBankIdStr,
        @RequestBody List<Long> questionIds) {
        Long questionBankId = IdUtils.parseId(questionBankIdStr, "题库ID");
        log.info("批量添加题目到题库，题库ID：{}，题目ID列表：{}", questionBankId, questionIds);
        return questionBankQuestionService.adminBatchAddQuestionsToBank(questionBankId, questionIds);
    }

    /**
     * 批量从题库移除题目
     *
     * @param questionBankIdStr 题库ID（字符串形式，避免大数精度丢失）
     * @param questionIds    题目ID列表
     * @return 移除结果
     */
    @DeleteMapping("/banks/{questionBankId}/questions/batch")
    @Operation(summary = "批量从题库移除题目", description = "由管理员批量移除指定题库与一组题目的关联关系")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Boolean> adminBatchRemoveQuestionsFromBank(
        @PathVariable("questionBankId") String questionBankIdStr,
        @RequestBody List<Long> questionIds) {
        Long questionBankId = IdUtils.parseId(questionBankIdStr, "题库ID");
        log.info("批量从题库移除题目，题库ID：{}，题目ID列表：{}", questionBankId, questionIds);
        return questionBankQuestionService.adminBatchRemoveQuestionsFromBank(questionBankId, questionIds);
    }
} 

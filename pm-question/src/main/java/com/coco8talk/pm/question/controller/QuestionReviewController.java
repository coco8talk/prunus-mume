package com.coco8talk.pm.question.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.question.model.dto.QueryQuestionReviewDTO;
import com.coco8talk.pm.question.model.dto.ReviewQuestionDTO;
import com.coco8talk.pm.question.model.vo.QuestionReviewVO;
import com.coco8talk.pm.question.model.vo.QuestionReviewWithDetailVO;
import com.coco8talk.pm.question.service.QuestionReviewService;
import com.coco8talk.pm.common.util.IdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 题目审核控制器
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@RestController
@RequestMapping("/question-reviews")
@Tag(name = "题目审核相关接口", description = "提供管理员题目审核、审核记录查询和待审核任务统计能力")
@Slf4j
public class QuestionReviewController {
    
    private final QuestionReviewService questionReviewService;
    
    public QuestionReviewController(QuestionReviewService questionReviewService) {
        this.questionReviewService = questionReviewService;
    }
    
    /**
     * 审核题目（管理员）
     *
     * @param reviewQuestionDTO 审核信息
     * @return 审核结果
     */
    @PostMapping("")
    @Operation(summary = "审核题目（管理员）", description = "由管理员提交题目审核结论、审核意见并同步更新题目状态")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Boolean> reviewQuestion(@RequestBody @Valid ReviewQuestionDTO reviewQuestionDTO) {
        log.info("审核题目请求: {}", reviewQuestionDTO);
        return questionReviewService.reviewQuestion(reviewQuestionDTO);
    }
    
    /**
     * 根据题目ID查询审核记录（管理员）
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 审核记录
     */
    @GetMapping("/questions/{questionId}")
    @Operation(summary = "ID查询（管理员）", description = "由管理员按题目标识查询该题目最新的审核记录")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<QuestionReviewVO> getReviewByQuestionId(
        @Parameter(description = "题目ID") @PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("根据题目ID查询审核记录: {}", questionId);
        return questionReviewService.getReviewByQuestionId(questionId);
    }
    
    /**
     * 分页查询审核记录（管理员）
     *
     * @param queryQuestionReviewDTO 查询条件
     * @return 审核记录分页
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询（管理员）", description = "由管理员按审核状态、审核人等条件分页查询审核记录")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Page<QuestionReviewVO>> adminQueryReviewPage(@RequestBody @Valid QueryQuestionReviewDTO queryQuestionReviewDTO) {
        log.info("分页查询审核记录: {}", queryQuestionReviewDTO);
        return questionReviewService.adminQueryReviewPage(queryQuestionReviewDTO);
    }
    
    /**
     * 获取待审核题目数量（管理员）
     *
     * @return 待审核题目数量
     */
    @GetMapping("/pending/count")
    @Operation(summary = "待审数量（管理员）", description = "统计当前处于待审核状态的题目数量")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Long> getPendingReviewCount() {
        log.info("获取待审核题目数量");
        return questionReviewService.getPendingReviewCount();
    }
    
    /**
     * 获取待审核题目列表（管理员）
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 待审核题目分页列表
     */
    @GetMapping("/pending")
    @Operation(summary = "待审核题目列表（管理员）", description = "分页查询待审核题目及其对应的审核详情")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Page<QuestionReviewWithDetailVO>> getPendingReviewList(
        @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
        @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取待审核题目列表: current={}, pageSize={}", current, pageSize);
        return questionReviewService.getPendingReviewList(current, pageSize);
    }
} 

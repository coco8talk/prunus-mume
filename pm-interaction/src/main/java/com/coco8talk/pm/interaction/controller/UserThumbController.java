package com.coco8talk.pm.interaction.controller;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.interaction.service.UserThumbService;
import com.coco8talk.pm.common.util.IdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户点赞控制类
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@RestController
@RequestMapping("/thumbs")
@Tag(name = "用户点赞相关接口", description = "提供当前用户点赞、取消点赞、点赞状态和题目点赞数查询能力")
@Slf4j
public class UserThumbController {
    
    private final UserThumbService userThumbService;
    
    public UserThumbController(UserThumbService userThumbService) {
        this.userThumbService = userThumbService;
    }
    
        /**
     * 点赞题目
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 点赞结果
     */
    @PostMapping("/questions/{questionId}")
    @Operation(summary = "点赞题目", description = "当前登录用户点赞指定的已存在题目，并同步增加题目点赞数")
    public Result<Boolean> thumbQuestion(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("用户点赞题目，questionId: {}", questionId);
        return userThumbService.thumbQuestion(questionId);
    }

    /**
     * 取消点赞题目
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 取消点赞结果
     */
    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "取消点赞题目", description = "当前登录用户取消对指定题目的点赞，并同步减少题目点赞数")
    public Result<Boolean> unThumbQuestion(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("用户取消点赞题目，questionId: {}", questionId);
        return userThumbService.unThumbQuestion(questionId);
    }

    /**
     * 检查用户是否已点赞题目
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 是否已点赞
     */
    @GetMapping("/questions/{questionId}/status")
    @Operation(summary = "检查是否已点赞题目", description = "检查当前登录用户是否已点赞指定题目")
    public Result<Boolean> hasThumbQuestion(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("检查用户是否已点赞题目，questionId: {}", questionId);
        return userThumbService.hasThumbQuestion(questionId);
    }

    /**
     * 获取题目的点赞数
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 点赞数
     */
    @GetMapping("/questions/{questionId}/count")
    @Operation(summary = "获取题目点赞数", description = "查询指定题目当前累计的点赞数量")
    public Result<Integer> getQuestionThumbCount(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("获取题目点赞数，questionId: {}", questionId);
        return userThumbService.getQuestionThumbCount(questionId);
    }
} 

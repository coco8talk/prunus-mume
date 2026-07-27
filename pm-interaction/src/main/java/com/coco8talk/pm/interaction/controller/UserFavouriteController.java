package com.coco8talk.pm.interaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.interaction.service.UserFavouriteService;
import com.coco8talk.pm.common.util.IdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 题目收藏控制类
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@RestController
@RequestMapping("/favourites")
@Tag(name = "用户收藏相关接口", description = "提供当前用户收藏、取消收藏、收藏状态和收藏题目分页查询能力")
@Slf4j
public class UserFavouriteController {
    
    private final UserFavouriteService userFavouriteService;
    
    public UserFavouriteController(UserFavouriteService userFavouriteService) {
        this.userFavouriteService = userFavouriteService;
    }
    
        /**
     * 收藏题目
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 收藏结果
     */
    @PostMapping("/questions/{questionId}")
    @Operation(summary = "收藏题目", description = "当前登录用户收藏指定的已存在题目，并同步增加题目收藏数")
    public Result<Boolean> favouriteQuestion(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("收藏题目，题目ID: {}", questionId);
        return userFavouriteService.favouriteQuestion(questionId);
    }

    /**
     * 取消收藏题目
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 取消收藏结果
     */
    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "取消收藏题目", description = "当前登录用户取消对指定题目的收藏，并同步减少题目收藏数")
    public Result<Boolean> unFavouriteQuestion(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("取消收藏题目，题目ID: {}", questionId);
        return userFavouriteService.unFavouriteQuestion(questionId);
    }
    
    /**
     * 检查用户是否已收藏题目
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 是否已收藏
     */
    @GetMapping("/questions/{questionId}/status")
    @Operation(summary = "检查是否已收藏题目", description = "检查当前登录用户是否已收藏指定题目")
    public Result<Boolean> hasFavouriteQuestion(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("检查是否已收藏题目，题目ID: {}", questionId);
        return userFavouriteService.hasFavouriteQuestion(questionId);
    }
    
    /**
     * 获取题目的收藏数
     *
     * @param questionIdStr 题目ID（字符串形式，避免大数精度丢失）
     * @return 收藏数
     */
    @GetMapping("/questions/{questionId}/count")
    @Operation(summary = "获取题目收藏数", description = "查询指定题目当前累计的收藏数量")
    public Result<Integer> getQuestionFavourCount(@PathVariable("questionId") String questionIdStr) {
        Long questionId = IdUtils.parseId(questionIdStr, "题目ID");
        log.info("获取题目收藏数，题目ID: {}", questionId);
        return userFavouriteService.getQuestionFavourCount(questionId);
    }
    
    /**
     * 获取用户收藏的题目列表
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 收藏的题目列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的收藏列表", description = "分页查询当前登录用户收藏且已审核通过的题目")
    public Result<Page<QuestionForBankVO>> getUserFavouriteQuestions(
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取用户收藏的题目列表，当前页: {}, 页面大小: {}", current, pageSize);
        return userFavouriteService.getUserFavouriteQuestions(current, pageSize);
    }
} 

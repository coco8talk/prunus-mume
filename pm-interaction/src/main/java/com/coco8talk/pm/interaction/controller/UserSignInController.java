package com.coco8talk.pm.interaction.controller;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.interaction.service.UserSignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户签到功能控制类
 *
 * @author coco8talk
 * @since 2025/7/8 20:23
 **/
@Slf4j
@RestController
@RequestMapping("/sign-ins")
@Tag(name = "用户签到功能相关接口", description = "提供当前用户签到、签到状态、连续天数及月度记录查询能力")
public class UserSignInController {
    
    private final UserSignInService userSignInService;
    
    public UserSignInController(UserSignInService userSignInService) {
        this.userSignInService = userSignInService;
    }
    
    /**
     * 用户签到接口
     *
     * @return Result<Boolean> 签到结果 true-表示今日首次签到成功 false-表示今日已经签过到了
     */
    @PostMapping
    @Operation(summary = "用户签到", description = "记录当前登录用户今日签到，并返回是否为今日首次签到")
    public Result<Boolean> signIn() {
        log.info("执行用户签到操作");
        return userSignInService.signIn();
    }
    
    /**
     * 获取连续签到天数
     *
     * @return Result<Integer> 连续签到天数
     */
    @GetMapping("/streak")
    @Operation(summary = "连续天数", description = "从今日向前统计当前登录用户跨月连续签到的天数")
    public Result<Integer> getContinuousSignInDays() {
        log.info("获取用户连续签到天数");
        return userSignInService.getContinuousSignInDays();
    }
    
    /**
     * 检查今天是否已经签到
     *
     * @return Result<Boolean> true-表示今日已签到 false-表示今日未签到
     */
    @GetMapping("/today")
    @Operation(summary = "检查今日签到", description = "检查当前登录用户今天是否已经完成签到")
    public Result<Boolean> checkTodaySignIn() {
        log.info("检查用户今天是否已经签到");
        return userSignInService.checkTodaySignIn();
    }
    
    /**
     * 获取当前月签到天数
     *
     * @return Result<Integer> 当前月签到天数
     */
    @GetMapping("/monthly-count")
    @Operation(summary = "获取当前月签到天数", description = "统计当前登录用户在当前自然月内的累计签到天数")
    public Result<Integer> getCurrentMonthSignInDays() {
        log.info("获取用户当前月份签到天数");
        return userSignInService.getCurrentMonthSignInDays();
    }
    
    /**
     * 获取具体某一年某一月的签到记录
     *
     * @param year  年份条件，不传默认当前年份
     * @param month 月份条件，不传默认当前月份
     * @return 签到记录列表（月内天数数组，如：[1, 5, 10, 15]）
     */
    @GetMapping("/records")
    @Operation(summary = "获取用户签到记录", description = "查询当前登录用户指定年月内已签到的日期列表，年月为空时使用当前年月")
    public Result<List<Integer>> getUserSignInRecords(
        @Parameter(description = "年份，不传默认当前年份") @RequestParam(value = "year", required = false) Integer year,
        @Parameter(description = "月份，不传默认当前月份") @RequestParam(value = "month", required = false) Integer month) {
        log.info("获取用户签到记录，年份：{}，月份：{}", year, month);
        return userSignInService.getUserSignInRecords(year, month);
    }
}
























































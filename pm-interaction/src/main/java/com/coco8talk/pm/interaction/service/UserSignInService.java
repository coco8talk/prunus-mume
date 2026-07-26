package com.coco8talk.pm.interaction.service;

import com.coco8talk.pm.common.Result;

import java.util.List;

/**
 * 用户登录签到功能服务接口
 * @author coco8talk
 * @since 2025/7/8 20:35
 **/
public interface UserSignInService {
    
    /**
     * 用户签到接口
     *
     * @return Result<Boolean> 签到结果 true-表示今日首次签到成功 false-表示今日已经签过到了
     */
    Result<Boolean> signIn();
    
    /**
     * 获取连续签到天数
     *
     * @return Result<Integer> 连续签到天数
     */
    Result<Integer> getContinuousSignInDays();
    
    /**
     * 检查今天是否已经签到
     *
     * @return Result<Boolean> true-表示今日已签到 false-表示今日未签到
     */
    Result<Boolean> checkTodaySignIn();
    
    /**
     * 获取当前月签到天数
     *
     * @return Result<Integer> 当前月签到天数
     */
    Result<Integer> getCurrentMonthSignInDays();
    
    /**
     * 获取具体某一年某一月的签到记录
     * @param year 年份条件，不传默认当前年份
     * @param month 月份条件，不传默认当前月份
     * @return 签到记录列表（月内天数数组，如：[1, 5, 10, 15]）
     */
    Result<List<Integer>> getUserSignInRecords(Integer year, Integer month);
}

package com.coco8talk.pm.interaction.service.impl;
import com.coco8talk.pm.interaction.service.UserSignInService;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.constant.CommonConstant;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.interaction.util.RedisKeyUtil;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户登录签到功能服务实现类
 *
 * @author coco8talk
 * @since 2025/7/8 20:35
 **/
@Service
@Slf4j
public class UserSignInServiceImpl implements UserSignInService {
    private final CurrentUserProvider currentUserProvider;
    private final RedissonClient redissonClient;
    private final DistributedLock distributedLock;

    /** 签到防重锁租约:3 秒。 */
    private static final long SIGN_IN_LOCK_LEASE_MILLIS = 3_000L;

    public UserSignInServiceImpl(CurrentUserProvider currentUserProvider,
                                 RedissonClient redissonClient,
                                 DistributedLock distributedLock) {
        this.currentUserProvider = currentUserProvider;
        this.redissonClient = redissonClient;
        this.distributedLock = distributedLock;
    }

    /**
     * 用户签到接口
     *
     * @return Result<Boolean> 签到结果 true-表示今日首次签到成功 false-表示今日已经签过到了
     */
    @Override
    public Result<Boolean> signIn() {
        // 1. 获取当前用户今日签到BitSet
        Long currentUserId = currentUserProvider.currentUserId();
        RBitSet bitSet = RedisKeyUtil.getCurrentUserTodayBitSet(currentUserId, redissonClient);

        // 2. 获取今天是本月第几天
        LocalDate now = LocalDate.now();
        int dayOfMonth = now.getDayOfMonth();
        int bitIndex = dayOfMonth - CommonConstant.BITSET_DAY_OFFSET;

        // 3. 分布式锁串行化"同一用户当天"的签到,保证"首次/重复"判定在并发下准确
        //    (BitSet 置位本身幂等,锁避免并发重复提交都返回 true)
        String lockKey = "lock:signin:" + currentUserId + ":" + now;
        return distributedLock.runExclusive(lockKey, 0, SIGN_IN_LOCK_LEASE_MILLIS, () -> {
            // 检查今天是否已经签到
            if (!bitSet.get(bitIndex)) {
                // 今天未签到，执行签到
                bitSet.set(bitIndex, true);
                // 设置过期时间：13个月（保留上年数据用于统计）
                bitSet.expire(Duration.ofDays(CommonConstant.SIGN_IN_DATA_RETAIN_MONTHS * 30L));

                log.info("用户 {} 在 {} 完成签到", currentUserId, now);
                return Result.success(HttpStatusEnum.OK, true);
            } else {
                // 今天已经签到
                log.info("用户 {} 在 {} 重复签到", currentUserId, now);
                return Result.success(HttpStatusEnum.OK, false);
            }
        });
    }

    /**
     * 获取连续签到天数（支持跨月查询）
     *
     * @return Result<Integer> 连续签到天数
     */
    @Override
    public Result<Integer> getContinuousSignInDays() {
        // 1. 获取当前操作用户的Id
        Long currentUserId = currentUserProvider.currentUserId();

        LocalDate currentDate = LocalDate.now();
        int continuousDays = 0;

        // 2. 从今天开始往前查找连续签到天数
        while (continuousDays < CommonConstant.MAX_CONTINUOUS_SIGN_IN_DAYS) {
            // 最多查询365天，防止无限循环
            String userSignInKey = RedisKeyUtil.getUserSinInKey(currentUserId, currentDate.getYear(), currentDate.getMonthValue());
            RBitSet bitSet = redissonClient.getBitSet(userSignInKey);

            // 检查当天是否签到
            if (bitSet.get(currentDate.getDayOfMonth() - CommonConstant.BITSET_DAY_OFFSET)) {
                continuousDays++;
                // 往前一天
                currentDate = currentDate.minusDays(1);
            } else {
                // 如果某天未签到，则连续签到中断
                break;
            }
        }

        log.info("用户 {} 连续签到 {} 天", currentUserId, continuousDays);
        return Result.success(HttpStatusEnum.OK, continuousDays);
    }

    /**
     * 检查今天是否已经签到
     *
     * @return Result<Boolean> true-表示今日已签到 false-表示今日未签到
     */
    @Override
    public Result<Boolean> checkTodaySignIn() {
        // 1. 获取当前用户今日签到BitSet
        Long currentUserId = currentUserProvider.currentUserId();
        RBitSet bitSet = RedisKeyUtil.getCurrentUserTodayBitSet(currentUserId, redissonClient);

        // 2. 获取今天是本月第几天并检查签到状态
        LocalDate now = LocalDate.now();
        int dayOfMonth = now.getDayOfMonth();
        boolean isSignedIn = bitSet.get(dayOfMonth - CommonConstant.BITSET_DAY_OFFSET);

        log.debug("检查用户 {} 在 {} 的签到状态: {}", currentUserId, now, isSignedIn ? "已签到" : "未签到");
        return Result.success(HttpStatusEnum.OK, isSignedIn);
    }

    /**
     * 获取当前月签到天数
     *
     * @return Result<Integer> 当前月签到天数
     */
    @Override
    public Result<Integer> getCurrentMonthSignInDays() {
        // 1. 获取当前操作用户的Id
        Long currentUserId = currentUserProvider.currentUserId();

        // 2. 获取用户签到的 BitSet
        LocalDate today = LocalDate.now();
        String userSignInKey = RedisKeyUtil.getUserSinInKey(currentUserId, today.getYear(), today.getMonthValue());
        RBitSet bitSet = redissonClient.getBitSet(userSignInKey);

        // 3. 获取当月的天数
        int daysInMonth = today.lengthOfMonth();

        // 4. 统计整个月的签到天数
        int signInDays = 0;
        for (int day = 1; day <= daysInMonth; day++) {
            // BitSet从0开始，日期从1开始
            if (bitSet.get(day - CommonConstant.BITSET_DAY_OFFSET)) {
                signInDays++;
            }
        }

        log.info("用户 {} 在 {}年{}月 共签到 {} 天", currentUserId, today.getYear(), today.getMonthValue(), signInDays);
        return Result.success(HttpStatusEnum.OK, signInDays);
    }

    /**
     * 获取具体某一年某一月的签到记录
     * @param year 年份条件，不传默认当前年份
     * @param month 月份条件，不传默认当前月份
     * @return 签到记录列表（月内天数数组，如：[1, 5, 10, 15]）
     */
    @Override
    public Result<List<Integer>> getUserSignInRecords(Integer year, Integer month) {
        // 1. 获取当前操作用户的ID
        Long currentUserId = currentUserProvider.currentUserId();

        // 2. 处理默认参数
        LocalDate today = LocalDate.now();
        if (year == null) {
            year = today.getYear();
        }
        if (month == null) {
            month = today.getMonthValue();
        }

        log.info("获取用户 {} 在 {}年{}月 的签到记录", currentUserId, year, month);

        // 3. 获取用户签到的 BitSet
        String userSignInKey = RedisKeyUtil.getUserSinInKey(currentUserId, year, month);
        RBitSet bitSet = redissonClient.getBitSet(userSignInKey);

        // 4. 获取当月的天数
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        int daysInMonth = firstDayOfMonth.lengthOfMonth();

        // 5. 遍历当月每一天，收集签到的天数
        List<Integer> signInDays = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            // BitSet从0开始，日期从1开始
            if (bitSet.get(day - CommonConstant.BITSET_DAY_OFFSET)) {
                signInDays.add(day);
            }
        }

        log.info("用户 {} 在 {}年{}月 共签到 {} 天: {}", currentUserId, year, month, signInDays.size(), signInDays);
        return Result.success(HttpStatusEnum.OK, signInDays);
    }

}

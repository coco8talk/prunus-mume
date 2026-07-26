package com.coco8talk.pm.interaction.util;

import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;

/**
 * redis 键值工具类
 * @author coco8talk
 * @since 2025/7/8 20:48
 **/
public class RedisKeyUtil {
    
    /**
     * 获取用户签到的 Redis 键
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return String 用户签到的 Redis 键
     */
    public static String getUserSinInKey(Long userId, int year, int month){
        
        ThrowUtils.throwIfFalse(userId > 0 && year > 2024 && month > 0 && month <= 12,
                HttpStatusEnum.BAD_REQUEST,
                "请输入合法的用户ID、年份和月份");
        
        return String.format("user:signin:%d:%d:%d", userId, year, month);
    }
    
    /**
     * 获取指定用户今日签到的BitSet
     * @param userId 用户ID
     * @param redissonClient Redis客户端
     * @return RBitSet 当前用户当月的签到BitSet
     */
    public static RBitSet getCurrentUserTodayBitSet(Long userId, RedissonClient redissonClient) {
        LocalDate today = LocalDate.now();
        String userSignInKey = getUserSinInKey(userId, today.getYear(), today.getMonthValue());
        return redissonClient.getBitSet(userSignInKey);
    }
}

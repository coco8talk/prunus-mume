package com.coco8talk.pm.common.constant;

/**
 * 全局常量字段
 *
 * @author coco8talk
 * @since 2025/6/24 18:47
 **/
public class CommonConstant {
    
    /**
     * 升序，用于 sql 查询时判断排序方式
     */
    public static final String ASC = "ascend";
    
    // ========== 加密相关常量 ==========
    
    /**
     * BCrypt加密轮数
     */
    public static final int BCRYPT_ROUNDS = 12;
    
    // ========== 锁相关常量 ==========
    
    /**
     * 分布式锁尝试获取锁的等待时间（秒）
     */
    public static final int LOCK_WAIT_TIME_SECONDS = 10;
    
    /**
     * 分布式锁自动释放时间（秒）
     */
    public static final int LOCK_LEASE_TIME_SECONDS = 30;
    
    /**
     * 用户账号锁前缀
     */
    public static final String USER_ACCOUNT_LOCK_PREFIX = "lock:userAccount:";
    
    // ========== 数据库相关常量 ==========
    
    /**
     * 逻辑删除-未删除状态
     */
    public static final int NOT_DELETED = 0;
    
    /**
     * SQL查询限制单条记录
     */
    public static final String LIMIT_ONE = "LIMIT 1";
    
    // ========== 分页相关常量 ==========
    
    /**
     * 默认浏览量初始值
     */
    public static final int DEFAULT_VIEW_COUNT = 1;
    
    /**
     * 最小浏览量（用于防止负数）
     */
    public static final int MIN_VIEW_COUNT = 0;
    
    // ========== 签到相关常量 ==========
    
    /**
     * 签到BitSet索引偏移量（因为BitSet从0开始，日期从1开始）
     */
    public static final int BITSET_DAY_OFFSET = 1;
    
    /**
     * 签到数据保留月数
     */
    public static final int SIGN_IN_DATA_RETAIN_MONTHS = 13;
    
    /**
     * 连续签到查询最大天数限制
     */
    public static final int MAX_CONTINUOUS_SIGN_IN_DAYS = 365;
    
    // ========== 分页查询常量 ==========
    
    /**
     * 分页查询每页最大数量
     */
    public static final int PAGE_SIZE_MAX = 20;
    
    /**
     * 排序字段最大长度
     */
    public static final int SORT_FIELD_MAX_LENGTH = 20;
    
    /**
     * 排序方式字段最大长度
     */
    public static final int SORT_ORDER_MAX_LENGTH = 10;
}

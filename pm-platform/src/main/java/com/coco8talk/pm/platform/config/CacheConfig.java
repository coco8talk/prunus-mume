package com.coco8talk.pm.platform.config;

import com.coco8talk.pm.common.cache.CacheStore;
import com.coco8talk.pm.common.cache.MultiLevelCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多级缓存 Bean 装配。把无状态的 {@link MultiLevelCache} 模板与生产远端
 * {@link CacheStore}(Redisson 实现)组合为可注入的命名 Bean。
 *
 * @author coco8talk
 */
@Configuration
public class CacheConfig {

    /** 本地 Caffeine 最大条目数。 */
    private static final long QUESTION_DETAIL_LOCAL_MAX_SIZE = 10_000L;

    /** 远端基础 TTL:5 分钟。 */
    private static final long QUESTION_DETAIL_BASE_TTL_MILLIS = 300_000L;

    /** 雪崩抖动比例:20%。 */
    private static final double QUESTION_DETAIL_JITTER_RATIO = 0.2;

    /** 题库本地条目数上限(题库基数远小于题目)。 */
    private static final long QUESTION_BANK_DETAIL_LOCAL_MAX_SIZE = 2_000L;

    /**
     * 题目详情读缓存:本地 Caffeine + Redis 两级,带穿透/击穿/雪崩防护。
     */
    @Bean
    public MultiLevelCache questionDetailCache(CacheStore cacheStore) {
        return new MultiLevelCache(
            cacheStore,
            QUESTION_DETAIL_LOCAL_MAX_SIZE,
            QUESTION_DETAIL_BASE_TTL_MILLIS,
            QUESTION_DETAIL_JITTER_RATIO);
    }

    /**
     * 题库详情读缓存:与题目详情同款两级缓存与三大防护,独立本地缓存实例便于单独容量控制。
     */
    @Bean
    public MultiLevelCache questionBankDetailCache(CacheStore cacheStore) {
        return new MultiLevelCache(
            cacheStore,
            QUESTION_BANK_DETAIL_LOCAL_MAX_SIZE,
            QUESTION_DETAIL_BASE_TTL_MILLIS,
            QUESTION_DETAIL_JITTER_RATIO);
    }
}

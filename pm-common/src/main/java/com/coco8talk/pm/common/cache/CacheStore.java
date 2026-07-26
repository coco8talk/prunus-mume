package com.coco8talk.pm.common.cache;

/**
 * 远端缓存存储抽象(窄接口),把 {@link MultiLevelCache} 与具体远端实现(Redis/Redisson)解耦,
 * 便于纯单元测试用内存实现替换。值统一为 String(由 {@link JsonCodec} 编解码)。
 *
 * @author coco8talk
 */
public interface CacheStore {

    /** 取值;不存在返回 {@code null}。 */
    String get(String key);

    /** 写值并设置过期(毫秒)。 */
    void set(String key, String value, long ttlMillis);

    /** 删除键(缓存失效)。 */
    void delete(String key);
}

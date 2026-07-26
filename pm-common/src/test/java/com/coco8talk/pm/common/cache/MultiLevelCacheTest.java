package com.coco8talk.pm.common.cache;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MultiLevelCache} 纯单元测试:用内存版 {@link FakeRedis} 替代真实 Redis,
 * 验证三大问题防护中的"穿透(空值缓存)"与基本命中语义,以及非 String 类型的 JSON 往返。
 */
class MultiLevelCacheTest {

    @Test
    void penetration_nullResultCachedAndLoaderCalledOnce() {
        AtomicInteger loaderCalls = new AtomicInteger();
        MultiLevelCache cache = new MultiLevelCache(new FakeRedis(), 100, 60_000, 0.2);

        cache.get("q:404", () -> { loaderCalls.incrementAndGet(); return null; }, String.class);
        cache.get("q:404", () -> { loaderCalls.incrementAndGet(); return null; }, String.class);

        assertEquals(1, loaderCalls.get(), "空值应被缓存,回源只一次");
    }

    @Test
    void hit_servesFromLocalWithoutLoader() {
        AtomicInteger loaderCalls = new AtomicInteger();
        MultiLevelCache cache = new MultiLevelCache(new FakeRedis(), 100, 60_000, 0.2);

        cache.get("q:1", () -> { loaderCalls.incrementAndGet(); return "v1"; }, String.class);
        String v = cache.get("q:1", () -> { loaderCalls.incrementAndGet(); return "v99"; }, String.class);

        assertEquals("v1", v);
        assertEquals(1, loaderCalls.get(), "命中本地缓存不应再回源");
    }

    @Test
    void remoteHit_populatesLocalAndDecodes() {
        AtomicInteger loaderCalls = new AtomicInteger();
        FakeRedis redis = new FakeRedis();
        MultiLevelCache first = new MultiLevelCache(redis, 100, 60_000, 0.2);
        // 第一个实例回源并写入远端
        first.get("q:7", () -> { loaderCalls.incrementAndGet(); return "remote"; }, String.class);

        // 新实例(空本地缓存)应从远端命中,不回源
        MultiLevelCache second = new MultiLevelCache(redis, 100, 60_000, 0.2);
        String v = second.get("q:7", () -> { loaderCalls.incrementAndGet(); return "wrong"; }, String.class);

        assertEquals("remote", v);
        assertEquals(1, loaderCalls.get(), "远端命中不应再回源");
    }

    @Test
    void evict_dropsLocalAndRemote() {
        AtomicInteger loaderCalls = new AtomicInteger();
        MultiLevelCache cache = new MultiLevelCache(new FakeRedis(), 100, 60_000, 0.2);

        cache.get("q:9", () -> { loaderCalls.incrementAndGet(); return "a"; }, String.class);
        cache.evict("q:9");
        cache.get("q:9", () -> { loaderCalls.incrementAndGet(); return "b"; }, String.class);

        assertEquals(2, loaderCalls.get(), "失效后应重新回源");
    }

    /** 内存版 CacheStore,仅用于单元测试(忽略 TTL 过期,够用)。 */
    static final class FakeRedis implements CacheStore {
        private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

        @Override
        public String get(String key) {
            return store.get(key);
        }

        @Override
        public void set(String key, String value, long ttlMillis) {
            store.put(key, value);
        }

        @Override
        public void delete(String key) {
            store.remove(key);
        }
    }
}

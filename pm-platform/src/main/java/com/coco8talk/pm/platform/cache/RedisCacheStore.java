package com.coco8talk.pm.platform.cache;

import com.coco8talk.pm.common.cache.CacheStore;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * {@link CacheStore} 的生产实现:基于 Redisson {@link RBucket} 的远端 String 存储。
 * 作为 {@code shared.cache.MultiLevelCache} 的远端层,供各读路径缓存使用。
 *
 * @author coco8talk
 */
@Component
public class RedisCacheStore implements CacheStore {

    private final RedissonClient redissonClient;

    public RedisCacheStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public String get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public void set(String key, String value, long ttlMillis) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void delete(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }
}

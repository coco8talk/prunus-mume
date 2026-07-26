package com.coco8talk.pm.common.lock;

import com.coco8talk.pm.common.BizException;
import com.coco8talk.pm.common.HttpStatusEnum;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的分布式互斥锁工具,用于跨实例的防重复提交 / 幂等保护。
 *
 * <p>典型用法:对"先查后写"这类竞态临界区加锁,把并发请求串行化,配合临界区内的
 * 幂等校验,保证同一业务键(如同一用户对同一题目点赞)最终只生效一次。
 *
 * @author coco8talk
 */
@Component
public class DistributedLock {

    private final RedissonClient redisson;

    public DistributedLock(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * 在分布式锁保护下执行 {@code action}:拿不到锁即快速失败(防重复提交);
     * 拿到锁则执行,{@code finally} 释放(仅当前线程持有时)。
     *
     * @param key         锁键(全局唯一标识临界区,如 {@code "lock:thumb:" + userId + ":" + questionId})
     * @param waitMillis  获取锁的最大等待时间(毫秒);传 0 表示不等待、拿不到立即失败
     * @param leaseMillis 锁租约时间(毫秒),到期自动释放,避免持有者崩溃导致死锁
     * @param action      临界区逻辑
     * @return action 的返回值
     */
    public <T> T runExclusive(String key, long waitMillis, long leaseMillis, Supplier<T> action) {
        RLock lock = redisson.getLock(key);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitMillis, leaseMillis, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new BizException(HttpStatusEnum.BAD_REQUEST.getCode(), "操作太频繁,请稍后再试");
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "获取锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

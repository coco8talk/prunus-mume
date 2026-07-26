package com.coco8talk.pm.interaction.service.impl;
import com.coco8talk.pm.interaction.service.UserThumbService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.interaction.mapper.UserThumbMapper;
import com.coco8talk.pm.interaction.model.entity.UserThumb;
import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户点赞service层实现类
 *
 * @author coco8talk
 * @description 针对表【user_thumb(用户点赞表)】的数据库操作Service实现
 * @createDate 2025-06-22 23:21:25
 */
@Service
@Slf4j
public class UserThumbServiceImpl extends ServiceImpl<UserThumbMapper, UserThumb>
        implements UserThumbService {

    private final CurrentUserProvider currentUserProvider;
    private final QuestionApi questionApi;
    private final DistributedLock distributedLock;

    /** 防重锁租约:5 秒,足够覆盖一次"查重 + 写入"临界区。 */
    private static final long THUMB_LOCK_LEASE_MILLIS = 5_000L;

    public UserThumbServiceImpl(QuestionApi questionApi,
                               CurrentUserProvider currentUserProvider,
                               DistributedLock distributedLock) {
        this.currentUserProvider = currentUserProvider;
        this.questionApi = questionApi;
        this.distributedLock = distributedLock;
    }

    /**
     * 点赞题目
     *
     * @param questionId 题目ID
     * @return 点赞结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> thumbQuestion(Long questionId) {
        // 验证题目是否存在
        DtoValidationUtil.validateIdNotNullFormat(questionId, "题目");
        ThrowUtils.throwIfFalse(questionApi.exists(questionId), HttpStatusEnum.BAD_REQUEST, "题目不存在");

        // 检查题目是否已审核通过
        if (!questionApi.isApproved(questionId)) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "该题目未通过审核");
        }

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 分布式锁串行化"同一用户对同一题目"的点赞,拿不到锁即快速失败(防重复提交)。
        // DB 侧 uk_user_question 唯一键是兜底,锁则把并发重复提交挡在写库之前,避免唯一键冲突churn。
        String lockKey = "lock:thumb:" + currentUserId + ":" + questionId;
        return distributedLock.runExclusive(lockKey, 0, THUMB_LOCK_LEASE_MILLIS, () -> {
            // 幂等校验:已点赞直接返回
            if (getUserThumbRecord(currentUserId, questionId) != null) {
                return Result.fail(HttpStatusEnum.BAD_REQUEST, "已经点赞过该题目");
            }

            // 创建点赞记录
            UserThumb userThumb = new UserThumb();
            userThumb.setUserId(currentUserId);
            userThumb.setQuestionId(questionId);
            userThumb.setCreateTime(LocalDateTime.now());

            boolean saveResult = this.save(userThumb);
            ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "点赞失败");

            // 更新题目点赞数
            questionApi.incrementThumbCount(questionId, 1);

            log.info("用户 {} 点赞题目 {}", currentUserId, questionId);
            return Result.success(HttpStatusEnum.OK, true);
        });
    }

    /**
     * 取消点赞题目
     *
     * @param questionId 题目ID
     * @return 取消点赞结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> unThumbQuestion(Long questionId) {
        // 验证题目是否存在
        DtoValidationUtil.validateIdNotNullFormat(questionId, "题目");
        ThrowUtils.throwIfFalse(questionApi.exists(questionId), HttpStatusEnum.BAD_REQUEST, "题目不存在");

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 检查是否已经点赞
        Result<Boolean> hasThumbResult = hasThumbQuestion(questionId);
        if (!hasThumbResult.getData()) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "尚未点赞该题目");
        }

        // 获取点赞记录
        UserThumb existingThumb = getUserThumbRecord(currentUserId, questionId);

        // 删除点赞记录
        boolean removeResult = this.removeById(existingThumb.getId());
        ThrowUtils.throwIfFalse(removeResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "取消点赞失败");

        // 更新题目点赞数
        questionApi.incrementThumbCount(questionId, -1);

        log.info("用户 {} 取消点赞题目 {}", currentUserId, questionId);
        return Result.success(HttpStatusEnum.OK, true);
    }

    /**
     * 检查用户是否已点赞题目
     *
     * @param questionId 题目ID
     * @return 是否已点赞
     */
    @Override
    public Result<Boolean> hasThumbQuestion(Long questionId) {
        // 参数校验
        DtoValidationUtil.validateIdFormat(questionId, "题目");

        // 检查用户是否登录
        if (!currentUserProvider.isLoggedIn()) {
            return Result.success(HttpStatusEnum.OK, false);
        }

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 检查是否已经点赞
        UserThumb existingThumb = getUserThumbRecord(currentUserId, questionId);

        return Result.success(HttpStatusEnum.OK, existingThumb != null);
    }

    /**
     * 获取题目的点赞数
     *
     * @param questionId 题目ID
     * @return 点赞数
     */
    @Override
    public Result<Integer> getQuestionThumbCount(Long questionId) {
        // 验证题目是否存在
        DtoValidationUtil.validateIdNotNullFormat(questionId, "题目");
        ThrowUtils.throwIfFalse(questionApi.exists(questionId), HttpStatusEnum.BAD_REQUEST, "题目不存在");

        return Result.success(HttpStatusEnum.OK, questionApi.getThumbCount(questionId));
    }

    /**
     * 获取用户点赞记录
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 点赞记录
     */
    private UserThumb getUserThumbRecord(Long userId, Long questionId) {
        LambdaQueryWrapper<UserThumb> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserThumb::getUserId, userId)
                .eq(UserThumb::getQuestionId, questionId);
        return this.getOne(wrapper);
    }

    /**
     * 根据题目ID删除所有点赞记录（用于题目删除时清理）
     *
     * @param questionId 题目ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllThumbsByQuestionId(Long questionId) {
        // 参数校验
        DtoValidationUtil.validateIdFormat(questionId, "题目");

        // 查询该题目的所有点赞记录
        LambdaQueryWrapper<UserThumb> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserThumb::getQuestionId, questionId);

        List<UserThumb> thumbList = this.list(wrapper);
        if (thumbList.isEmpty()) {
            log.info("题目 {} 没有点赞记录，无需删除", questionId);
            Result.success(HttpStatusEnum.OK, true);
            return;
        }

        // 删除所有点赞记录
        boolean removeResult = this.remove(wrapper);
        ThrowUtils.throwIfFalse(removeResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "删除题目点赞记录失败");

        log.info("删除题目 {} 的所有点赞记录，共删除 {} 条记录", questionId, thumbList.size());
        Result.success(HttpStatusEnum.OK, true);
    }
}

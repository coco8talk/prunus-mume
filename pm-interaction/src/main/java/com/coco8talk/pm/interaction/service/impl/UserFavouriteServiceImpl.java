package com.coco8talk.pm.interaction.service.impl;
import com.coco8talk.pm.interaction.service.UserFavouriteService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.interaction.mapper.UserFavouriteMapper;
import com.coco8talk.pm.interaction.model.entity.UserFavourite;
import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目收藏service层实现类
 *
 * @author coco8talk
 * @description 针对表【user_favourite(用户收藏表)】的数据库操作Service实现
 * @createDate 2025-06-22 23:21:23
 */
@Service
@Slf4j
public class UserFavouriteServiceImpl extends ServiceImpl<UserFavouriteMapper, UserFavourite>
    implements UserFavouriteService {

    private final CurrentUserProvider currentUserProvider;
    private final QuestionApi questionApi;
    private final DistributedLock distributedLock;

    /** 防重锁租约:5 秒,足够覆盖一次"查重 + 写入"临界区。 */
    private static final long FAVOUR_LOCK_LEASE_MILLIS = 5_000L;

    public UserFavouriteServiceImpl(QuestionApi questionApi,
                                    CurrentUserProvider currentUserProvider,
                                    DistributedLock distributedLock) {
        this.currentUserProvider = currentUserProvider;
        this.questionApi = questionApi;
        this.distributedLock = distributedLock;
    }

    /**
     * 收藏题目
     *
     * @param questionId 题目ID
     * @return 收藏结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> favouriteQuestion(Long questionId) {
        // 验证题目是否存在
        DtoValidationUtil.validateIdNotNullFormat(questionId, "题目");
        ThrowUtils.throwIfFalse(questionApi.exists(questionId), HttpStatusEnum.BAD_REQUEST, "题目不存在");

        // 检查题目是否已审核通过
        if (!questionApi.isApproved(questionId)) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "该题目未通过审核");
        }

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 分布式锁串行化"同一用户对同一题目"的收藏,拿不到锁即快速失败(防重复提交)。
        String lockKey = "lock:favour:" + currentUserId + ":" + questionId;
        return distributedLock.runExclusive(lockKey, 0, FAVOUR_LOCK_LEASE_MILLIS, () -> {
            // 幂等校验:已收藏直接返回
            if (getUserFavouriteRecord(currentUserId, questionId) != null) {
                return Result.fail(HttpStatusEnum.BAD_REQUEST, "已经收藏过该题目");
            }

            // 创建收藏记录
            UserFavourite userFavourite = new UserFavourite();
            userFavourite.setUserId(currentUserId);
            userFavourite.setQuestionId(questionId);
            userFavourite.setCreateTime(LocalDateTime.now());

            boolean saveResult = this.save(userFavourite);
            ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "收藏失败");

            // 更新题目收藏数
            questionApi.incrementFavourCount(questionId, 1);

            log.info("用户 {} 收藏题目 {}", currentUserId, questionId);
            return Result.success(HttpStatusEnum.OK, true);
        });
    }

    /**
     * 取消收藏题目
     *
     * @param questionId 题目ID
     * @return 取消收藏结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> unFavouriteQuestion(Long questionId) {
        // 验证题目是否存在
        DtoValidationUtil.validateIdNotNullFormat(questionId, "题目");
        ThrowUtils.throwIfFalse(questionApi.exists(questionId), HttpStatusEnum.BAD_REQUEST, "题目不存在");

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 检查是否已经收藏（复用现有方法）
        Result<Boolean> hasFavouriteResult = hasFavouriteQuestion(questionId);
        if (!hasFavouriteResult.getData()) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "尚未收藏该题目");
        }

        // 获取收藏记录
        UserFavourite existingFavourite = getUserFavouriteRecord(currentUserId, questionId);

        // 删除收藏记录
        boolean removeResult = this.removeById(existingFavourite.getId());
        ThrowUtils.throwIfFalse(removeResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "取消收藏失败");

        // 更新题目收藏数
        questionApi.incrementFavourCount(questionId, -1);

        log.info("用户 {} 取消收藏题目 {}", currentUserId, questionId);
        return Result.success(HttpStatusEnum.OK, true);
    }

    /**
     * 检查用户是否已收藏题目
     *
     * @param questionId 题目ID
     * @return 是否已收藏
     */
    @Override
    public Result<Boolean> hasFavouriteQuestion(Long questionId) {
        // 参数校验
        DtoValidationUtil.validateIdFormat(questionId, "题目");

        // 检查用户是否登录
        if (!currentUserProvider.isLoggedIn()) {
            return Result.success(HttpStatusEnum.OK, false);
        }

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 检查是否已经收藏
        UserFavourite existingFavourite = getUserFavouriteRecord(currentUserId, questionId);

        return Result.success(HttpStatusEnum.OK, existingFavourite != null);
    }

    /**
     * 获取题目的收藏数
     *
     * @param questionId 题目ID
     * @return 收藏数
     */
    @Override
    public Result<Integer> getQuestionFavourCount(Long questionId) {
        // 验证题目是否存在
        DtoValidationUtil.validateIdNotNullFormat(questionId, "题目");
        ThrowUtils.throwIfFalse(questionApi.exists(questionId), HttpStatusEnum.BAD_REQUEST, "题目不存在");

        return Result.success(HttpStatusEnum.OK, questionApi.getFavourCount(questionId));
    }

    /**
     * 获取用户收藏的题目列表
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 收藏的题目列表
     */
    @Override
    public Result<Page<QuestionForBankVO>> getUserFavouriteQuestions(Integer current, Integer pageSize) {
        // 参数校验
        DtoValidationUtil.validatePageCurrentFormat(current);
        DtoValidationUtil.validatePageSizeFormat(pageSize);

        // 获取当前用户
        Long currentUserId = currentUserProvider.currentUserId();

        // 查询用户收藏的题目ID列表
        LambdaQueryWrapper<UserFavourite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavourite::getUserId, currentUserId)
               .orderByDesc(UserFavourite::getCreateTime);

        Page<UserFavourite> favouritePage = this.page(new Page<>(current, pageSize), wrapper);

        // 获取题目ID列表
        List<Long> questionIds = favouritePage.getRecords().stream()
                                              .map(UserFavourite::getQuestionId)
                                              .collect(Collectors.toList());

        //根据题目Id列表获取题目简要信息
        Page<QuestionForBankVO> questionForBankVoPage =
            questionApi.queryApprovedForBank(
                questionIds, current, pageSize, favouritePage.getTotal());

        return Result.success(HttpStatusEnum.OK, questionForBankVoPage);
    }

    /**
     * 获取用户收藏记录
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 收藏记录
     */
    private UserFavourite getUserFavouriteRecord(Long userId, Long questionId) {
        LambdaQueryWrapper<UserFavourite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavourite::getUserId, userId)
               .eq(UserFavourite::getQuestionId, questionId);
        return this.getOne(wrapper);
    }

    /**
     * 根据题目ID删除所有收藏记录（用于题目删除时清理）
     *
     * @param questionId 题目ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllFavouritesByQuestionId(Long questionId) {
        // 参数校验
        DtoValidationUtil.validateIdFormat(questionId, "题目");

        // 查询该题目的所有收藏记录
        LambdaQueryWrapper<UserFavourite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavourite::getQuestionId, questionId);

        List<UserFavourite> favouriteList = this.list(wrapper);
        if (favouriteList.isEmpty()) {
            log.info("题目 {} 没有收藏记录，无需删除", questionId);
            Result.success(HttpStatusEnum.OK, true);
            return;
        }

        // 删除所有收藏记录
        boolean removeResult = this.remove(wrapper);
        ThrowUtils.throwIfFalse(removeResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "删除题目收藏记录失败");

        log.info("删除题目 {} 的所有收藏记录，共删除 {} 条记录", questionId, favouriteList.size());
        Result.success(HttpStatusEnum.OK, true);
    }
}

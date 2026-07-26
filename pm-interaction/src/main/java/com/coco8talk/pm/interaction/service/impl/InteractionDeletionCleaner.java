package com.coco8talk.pm.interaction.service.impl;

import com.coco8talk.pm.interaction.mapper.UserFavouriteMapper;
import com.coco8talk.pm.interaction.mapper.UserThumbMapper;
import com.coco8talk.pm.interaction.service.UserFavouriteService;
import com.coco8talk.pm.interaction.service.UserThumbService;
import com.coco8talk.pm.api.question.service.QuestionDeletionCleaner;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link QuestionDeletionCleaner} 的 interaction 模块实现。
 * 封装题目删除时对点赞 / 收藏记录的级联清理，由 question 模块通过同步清理端口回调，
 * 避免 question 模块直接依赖 interaction 模块的内部实现类。
 *
 * @author coco8talk
 */
@Component
@Slf4j
public class InteractionDeletionCleaner implements QuestionDeletionCleaner {

    private final UserThumbService userThumbService;
    private final UserFavouriteService userFavouriteService;
    private final UserThumbMapper userThumbMapper;
    private final UserFavouriteMapper userFavouriteMapper;

    public InteractionDeletionCleaner(UserThumbService userThumbService,
                                      UserFavouriteService userFavouriteService,
                                      UserThumbMapper userThumbMapper,
                                      UserFavouriteMapper userFavouriteMapper) {
        this.userThumbService = userThumbService;
        this.userFavouriteService = userFavouriteService;
        this.userThumbMapper = userThumbMapper;
        this.userFavouriteMapper = userFavouriteMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onQuestionDeleted(Long questionId) {
        userThumbService.deleteAllThumbsByQuestionId(questionId);
        userFavouriteService.deleteAllFavouritesByQuestionId(questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onQuestionsDeleted(List<Long> questionIds) {
        // 批量删除题目点赞记录
        List<Long> existsQtIds = userThumbMapper.selectExistsThumbsByQuestionIds(questionIds);
        if (existsQtIds.isEmpty()) {
            log.info("没有找到与题目关联的点赞记录，跳过删除点赞记录");
        }
        int deleteThumbResult = userThumbMapper.deleteByIds(existsQtIds);
        ThrowUtils.throwIfFalse(deleteThumbResult <= 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量删除题目点赞记录失败");

        // 批量删除题目收藏记录
        List<Long> existsQfIds = userFavouriteMapper.selectExistsFavouritesByQuestionIds(questionIds);
        if (existsQfIds.isEmpty()) {
            log.info("没有找到与题目关联的收藏记录，跳过删除收藏记录");
        }
        int deleteFavouriteResult = userFavouriteMapper.deleteByIds(existsQfIds);
        ThrowUtils.throwIfFalse(deleteFavouriteResult <= 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量删除题目收藏记录失败");
    }
}

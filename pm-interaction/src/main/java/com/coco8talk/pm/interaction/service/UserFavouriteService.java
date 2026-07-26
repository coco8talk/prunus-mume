package com.coco8talk.pm.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.interaction.model.entity.UserFavourite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;

/**
 * 题目收藏service层
 *
 * @author coco8talk
 * @description 针对表【user_favourite(用户收藏表)】的数据库操作Service
 * @createDate 2025-06-22 23:21:23
 */
public interface UserFavouriteService extends IService<UserFavourite> {
    
    /**
     * 收藏题目
     *
     * @param questionId 题目ID
     * @return 收藏结果
     */
    Result<Boolean> favouriteQuestion(Long questionId);
    
    /**
     * 取消收藏题目
     *
     * @param questionId 题目ID
     * @return 取消收藏结果
     */
    Result<Boolean> unFavouriteQuestion(Long questionId);
    
    /**
     * 检查用户是否已收藏题目
     *
     * @param questionId 题目ID
     * @return 是否已收藏
     */
    Result<Boolean> hasFavouriteQuestion(Long questionId);
    
    /**
     * 获取题目的收藏数
     *
     * @param questionId 题目ID
     * @return 收藏数
     */
    Result<Integer> getQuestionFavourCount(Long questionId);
    
    /**
     * 获取用户收藏的题目列表
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 收藏的题目列表
     */
    Result<Page<QuestionForBankVO>> getUserFavouriteQuestions(Integer current, Integer pageSize);
    
    /**
     * 根据题目ID删除所有收藏记录（用于题目删除时清理）
     *
     * @param questionId 题目ID
     */
    void deleteAllFavouritesByQuestionId(Long questionId);
}

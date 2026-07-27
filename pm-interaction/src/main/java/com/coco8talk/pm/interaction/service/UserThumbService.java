package com.coco8talk.pm.interaction.service;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.interaction.model.entity.UserThumb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户点赞service层
 *
 * @author coco8talk
 * @description 针对表【user_thumb(用户点赞表)】的数据库操作Service
 * @createDate 2025-06-22 23:21:25
 */
public interface UserThumbService extends IService<UserThumb> {
    
    /**
     * 点赞题目
     *
     * @param questionId 题目ID
     * @return 点赞结果
     */
    Result<Boolean> thumbQuestion(Long questionId);
    
    /**
     * 取消点赞题目
     *
     * @param questionId 题目ID
     * @return 取消点赞结果
     */
    Result<Boolean> unThumbQuestion(Long questionId);
    
    /**
     * 检查用户是否已点赞题目
     *
     * @param questionId 题目ID
     * @return 是否已点赞
     */
    Result<Boolean> hasThumbQuestion(Long questionId);
    
    /**
     * 获取题目的点赞数
     *
     * @param questionId 题目ID
     * @return 点赞数
     */
    Result<Integer> getQuestionThumbCount(Long questionId);
    
    /**
     * 根据题目ID删除所有点赞记录（用于题目删除时清理）
     *
     * @param questionId 题目ID
     */
    void deleteAllThumbsByQuestionId(Long questionId);
}

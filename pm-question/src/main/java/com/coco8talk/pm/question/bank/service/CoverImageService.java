package com.coco8talk.pm.question.bank.service;

import com.coco8talk.pm.common.result.Result;

/**
 * 题库封面图片生成与存储服务。
 * 使用题库名称生成文字图像，上传至腾讯云 COS，并回写 URL 到题库记录。
 *
 * @author coco8talk
 */
public interface CoverImageService {

    /**
     * 为指定题库生成封面图片。
     *
     * @param bankId 题库 ID
     * @return 生成的封面 COS URL
     */
    Result<String> generateAndSave(Long bankId);

    /**
     * 批量为所有没有封面的题库生成封面图片。
     *
     * @return 处理的题库数量
     */
    Result<Integer> generateForAllBanks();
}

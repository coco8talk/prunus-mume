package com.coco8talk.pm.question.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.question.model.entity.QuestionReview;
import com.coco8talk.pm.question.model.vo.QuestionReviewVO;
import com.coco8talk.pm.question.model.vo.QuestionReviewWithDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 题目审核MapStruct映射
 *
 * @author coco8talk
 * @since 2025/1/20
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionReviewMapStruct {
    
    QuestionReviewMapStruct INSTANCE = Mappers.getMapper(QuestionReviewMapStruct.class);
    
    /**
     * 实体转VO
     *
     * @param questionReview 待转换的实体
     * @return 转换结果
     */
    QuestionReviewVO entityToVo(QuestionReview questionReview);
    
    /**
     * 实体列表转VO列表
     *
     * @param questionReviewList 待转换的实体列表
     * @return 转换结果列表
     */
    List<QuestionReviewVO> entityListToVoList(List<QuestionReview> questionReviewList);
    
    /**
     * 实体分页转VO分页
     *
     * @param entityPage 待转换的实体分页
     * @return 转换结果
     */
    Page<QuestionReviewVO> entityPageToVoPage(Page<QuestionReview> entityPage);
    
    /**
     * QuestionReview转QuestionReviewWithDetailVO（基础映射）
     *
     * @param questionReview 审核记录实体
     * @return 审核详情VO
     */
    QuestionReviewWithDetailVO reviewToReviewWithDetailVO(QuestionReview questionReview);
}
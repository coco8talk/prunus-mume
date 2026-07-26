package com.coco8talk.pm.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coco8talk.pm.question.model.entity.QuestionReview;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目审核表数据库操作Mapper
 *
 * @author ASUS
 * @description 针对表【question_review(题目审核表)】的数据库操作Mapper
 * @createDate 2025-07-05 19:58:33
 * @Entity com.coco8talk.pm.question.model.entity.QuestionReview
 */
public interface QuestionReviewMapper extends BaseMapper<QuestionReview> {
    
    /**
     * 查询题目审核Id列表，判断题目审核是否存在
     *
     * @param existsQuestionIds 题目Id列表
    * @return 存在的题目审核Id列表
    */
    @Select("<script>" +
            "select id from question_review where question_id in " +
            "<foreach collection='existsQuestionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> listExistsReviewIdsByQuestionIds(List<Long> existsQuestionIds);
}




package com.coco8talk.pm.question.bank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coco8talk.pm.question.bank.model.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题库-题目关联表数据库操作Mapper
 *
 * @author coco8talk
 * @description 针对表【question_bank_question(题库-题目关联表)】的数据库操作Mapper
 * @createDate 2025-06-22 23:21:17
 * @Entity com.coco8talk.pm.question.bank.model.entity.QuestionBankQuestion
 */
@Mapper
public interface QuestionBankQuestionMapper extends BaseMapper<QuestionBankQuestion> {
    
    /**
     * 查询题库-题目关联表中存在的题目Id列表
     *
     * @param existsQuestionIds 题目Id列表
    * @return 存在的题目Id列表
    */
    @Select("<script>" +
            "select id from question_bank_question where question_id in " +
            "<foreach collection='existsQuestionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> listExistsQbqIdsByQuestionIds(List<Long> existsQuestionIds);
}




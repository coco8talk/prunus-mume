package com.coco8talk.pm.question.bank.mapper;

import com.coco8talk.pm.question.bank.model.entity.QuestionBank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库表数据库操作Mapper
 *
 * @author coco8talk
 * @description 针对表【question_bank(题库表)】的数据库操作Mapper
 * @createDate 2025-06-22 23:21:13
 * @Entity com.coco8talk.pm.question.bank.model.entity.QuestionBank
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

}





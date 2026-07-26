package com.coco8talk.pm.question.bank.convert;

import com.coco8talk.pm.question.bank.model.dto.CreateQbqDTO;
import com.coco8talk.pm.question.bank.model.entity.QuestionBankQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 题目-题库转换接口
 *
 * @author coco8talk
 * @since 2025/6/28 17:50
 **/
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionBankQuestionMapStruct {
    QuestionBankQuestionMapStruct INSTANCE = Mappers.getMapper(QuestionBankQuestionMapStruct.class);
    
    /**
     * 将 CreateQbqDTO 对象转换为 QuestionBankQuestion
     *
     * @param createQbqDTO 待转换的CreateQbqDTO对象
     * @return 转化完成的QuestionBankQuestion对象
     */
    QuestionBankQuestion createDtoToEntity(CreateQbqDTO createQbqDTO);
}

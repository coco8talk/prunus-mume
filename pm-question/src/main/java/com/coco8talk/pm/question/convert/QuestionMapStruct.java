package com.coco8talk.pm.question.convert;

import com.coco8talk.pm.question.model.dto.CreateQuestionDTO;
import com.coco8talk.pm.question.model.dto.EditQuestionDTO;
import com.coco8talk.pm.question.model.entity.Question;
import com.coco8talk.pm.question.model.vo.QuestionForAdminVO;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.question.model.vo.QuestionVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 题目相关对象映射接口
 *
 * @author coco8talk
 * @since 2025/6/27 18:47
 **/
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapStruct {

    QuestionMapStruct INSTANCE = Mappers.getMapper(QuestionMapStruct.class);

    Question addDtoToEntity(CreateQuestionDTO createQuestionDTO);

    Question editDtoToEntity(EditQuestionDTO editQuestionDTO);

    QuestionVO entityToVo(Question questionById);

    QuestionForAdminVO entityToAdminVo(Question question);

    QuestionForBankVO entityToForBankVo(Question question);

    List<QuestionForBankVO> entityListToForBankVoList(List<Question> questions);
}

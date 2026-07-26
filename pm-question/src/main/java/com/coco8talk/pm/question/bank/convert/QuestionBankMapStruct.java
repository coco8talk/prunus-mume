package com.coco8talk.pm.question.bank.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.question.bank.model.dto.AddQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.EditQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.entity.QuestionBank;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankDetailVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankForAdminVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 题库对象映射转换接口
 *
 * @author coco8talk
 * @since 2025/6/26 22:18
 **/
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionBankMapStruct {
    
    QuestionBankMapStruct INSTANCE = Mappers.getMapper(QuestionBankMapStruct.class);
    
    /**
     * 将 AddQuestionBankDTO 转换为 QuestionBank
     *
     * @param addQuestionBankDTO 待转换的对象
     * @return 转换得到的 QuestionBank
     */
    QuestionBank addQuestionBankDtoToQuestionBank(AddQuestionBankDTO addQuestionBankDTO);
    
    /**
     * 将 EditQuestionBankDTO 转换为 QuestionBank
     *
     * @param editQuestionBankDTO 待转换的对象
     * @return 转换得到的 QuestionBank
     */
    QuestionBank editDtoToEntity(EditQuestionBankDTO editQuestionBankDTO);
    
    /**
     * 将 QuestionBank 转换为 QuestionBankVO
     *
     * @param questionBank 待转换的对象
     * @return 转换得到的 QuestionBankVO
     */
    QuestionBankDetailVO entityToVo(QuestionBank questionBank);
    
    /**
     * 将Page(QuestionBank)转换为Page(QuestionBankVO)
     * @param questionBankPage 题库分页对象
     * @return 转换后的题库分页对象
     */
    Page<QuestionBankVO> entityPageToVoPage(Page<QuestionBank> questionBankPage);
    
    /**
     * 将 QuestionBank 转换为 QuestionBankForAdminVO
     *
     * @param questionBank 待转换的对象
     * @return 转换得到的 QuestionBankForAdminVO
     */
    QuestionBankForAdminVO entityToForAdminVo(QuestionBank questionBank);
}

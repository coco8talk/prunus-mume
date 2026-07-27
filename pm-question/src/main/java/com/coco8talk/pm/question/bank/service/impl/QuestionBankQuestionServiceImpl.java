package com.coco8talk.pm.question.bank.service.impl;
import com.coco8talk.pm.question.bank.service.QuestionBankQuestionService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.question.bank.convert.QuestionBankQuestionMapStruct;
import com.coco8talk.pm.question.bank.model.dto.CreateQbqDTO;
import com.coco8talk.pm.question.bank.mapper.QuestionBankMapper;
import com.coco8talk.pm.question.bank.model.entity.QuestionBankQuestion;
import com.coco8talk.pm.question.bank.mapper.QuestionBankQuestionMapper;
import com.coco8talk.pm.question.model.entity.Question;
import com.coco8talk.pm.question.mapper.QuestionMapper;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.api.user.service.UserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目-题库服务层实现类
 *
 * @author coco8talk
 * @description 针对表【question_bank_question(题库-题目关联表)】的数据库操作Service实现
 * @createDate 2025-06-22 23:21:17
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion>
    implements QuestionBankQuestionService {
    
    private final QuestionBankMapper questionBankMapper;
    private final QuestionMapper questionMapper;
    private final CurrentUserProvider currentUserProvider;
    private final QuestionApi questionApi;
    private final UserApi userApi;

    public QuestionBankQuestionServiceImpl(QuestionBankMapper questionBankMapper,
                                           QuestionMapper questionMapper,
                                           QuestionApi questionApi,
                                           CurrentUserProvider currentUserProvider,
                                           UserApi userApi) {
        this.questionBankMapper = questionBankMapper;
        this.questionMapper = questionMapper;
        this.currentUserProvider = currentUserProvider;
        this.questionApi = questionApi;
        this.userApi = userApi;
    }
    
    /**
     * 将 题目-题库关联信息写入关联表
     *
     * @param createQbqDTO 关联信息封装类
     */
    @Override
    public void adminCreateQbq(CreateQbqDTO createQbqDTO) {
        //1.检查参数是否合法并判断是否存在
        Long questionId = createQbqDTO.getQuestionId();
        DtoValidationUtil.validateEntityExists(questionId, questionMapper::selectById, "题目");
        Long createUserId = createQbqDTO.getCreateUserId();
        DtoValidationUtil.validateEntityExists(createUserId, userApi::getById, "用户");
        Long questionBankId = createQbqDTO.getQuestionBankId();
        DtoValidationUtil.validateEntityExists(questionBankId, questionBankMapper::selectById, "题库");
        //2.根据 questionId 和 questionBankId 判断关联信息是否存在
        LambdaQueryWrapper<QuestionBankQuestion> wrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                                                                   .eq(QuestionBankQuestion::getQuestionId, questionId)
                                                                   .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                                                                   .select(QuestionBankQuestion::getId);
        QuestionBankQuestion bqbFromDb = this.getOne(wrapper);
        ThrowUtils.throwIfNotNull(bqbFromDb, HttpStatusEnum.BAD_REQUEST, "当前题目已存在于此题库");
        //3.写入
        QuestionBankQuestion qbqToDb = QuestionBankQuestionMapStruct.INSTANCE.createDtoToEntity(createQbqDTO);
        boolean saveResult = this.save(qbqToDb);
        ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "关联信息写入数据库失败");
    }
    
    /**
     * 根据 题目Id 删除 题目-题库关联信息
     *
     * @param id 题目Id
     */
    @Override
    public void deleteQbqByQuestionId(Long id) {
        //1.检查题目Id是否合法
        DtoValidationUtil.validateIdFormat(id, "题目");
        
        //2.执行删除操作
        LambdaQueryWrapper<QuestionBankQuestion> wrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                                                                   .eq(QuestionBankQuestion::getQuestionId, id);
        boolean removeResult = this.remove(wrapper);
        ThrowUtils.throwIfFalse(removeResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "删除关联信息失败");
    }
    
    /**
     * 根据题库ID获取题目列表（用于题库查询）
     *
     * @param questionBankId 题库ID
     * @param current        当前页
     * @param pageSize       页面大小
     * @return 题目列表（QuestionForBankVO格式）
     */
    @Override
    public Result<Page<QuestionForBankVO>> getQuestionsForBankById(Long questionBankId, Integer current, Integer pageSize) {
        // 参数校验
        DtoValidationUtil.validatePageCurrentFormat(current);
        DtoValidationUtil.validatePageSizeFormat(pageSize);
        // 检查题库是否存在
        DtoValidationUtil.validateEntityExists(questionBankId, questionBankMapper::selectById, "题库");
        
        // 查询题库中的题目ID列表
        LambdaQueryWrapper<QuestionBankQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
               .orderByAsc(QuestionBankQuestion::getSortOrder)
               .orderByDesc(QuestionBankQuestion::getCreateTime);
        
        Page<QuestionBankQuestion> qbqPage = this.page(new Page<>(current, pageSize), wrapper);
        
        // 获取题目ID列表
        List<Long> questionIds =
            qbqPage.getRecords().stream()
                   .map(QuestionBankQuestion::getQuestionId)
                   .distinct()
                   .collect(Collectors.toList());
        
        //根据题目Id列表获取题目简要信息
        Page<QuestionForBankVO> questionForBankVoPage =
            questionApi.queryApprovedForBank(
                questionIds, current, pageSize, qbqPage.getTotal());
        
        return Result.success(HttpStatusEnum.OK, questionForBankVoPage);
    }
    
    /**
     * 批量添加题目到题库
     *
     * @param questionBankId 题库ID
     * @param questionIds    题目ID列表
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> adminBatchAddQuestionsToBank(Long questionBankId, List<Long> questionIds) {
        // 通用的参数校验和去重处理
        Set<Long> existingQuestionIdSet = validateAndDeduplicateQuestionIds(questionBankId, questionIds);
        if (existingQuestionIdSet == null) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "题目ID列表中没有有效的题目");
        }
        
        // 1.获取当前用户Id
        Long currentUserId = currentUserProvider.currentUserId();
        
        // 3.查询已存在的关联关系
        LambdaQueryWrapper<QuestionBankQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
               .in(QuestionBankQuestion::getQuestionId, existingQuestionIdSet)
               .select(QuestionBankQuestion::getQuestionId);
        List<QuestionBankQuestion> existingRelations = this.list(wrapper);
        
        // 4.使用HashSet进行O(1)查找，根据题目 Id与查出的关联 过滤 已存在的题目-题库关联表
        Set<Long> existingRelationQuestionIdSet = existingRelations.stream()
                                                                   .map(QuestionBankQuestion::getQuestionId)
                                                                   .collect(Collectors.toSet());
        List<Long> newQuestionIds = existingQuestionIdSet.stream()
                                                         .filter(id -> !existingRelationQuestionIdSet.contains(id))
                                                         .toList();
        
        if (newQuestionIds.isEmpty()) {
            log.warn("题目ID列表中没有可以添加的题目: {}", questionIds);
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "题目ID列表中没有有效的题目");
        }
        
        // 5.批量创建关联记录
        List<QuestionBankQuestion> newRelations =
            newQuestionIds.stream()
                            .map(questionId -> {
                                QuestionBankQuestion qbq = new QuestionBankQuestion();
                                qbq.setQuestionBankId(questionBankId);
                                qbq.setQuestionId(questionId);
                                qbq.setCreateUserId(currentUserId);
                                qbq.setCreateTime(LocalDateTime.now());
                                return qbq;
                            })
                            .collect(Collectors.toList());
        try {
            boolean batchResult = this.saveBatch(newRelations);
            ThrowUtils.throwIfFalse(batchResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量添加题目失败");
        } catch (Exception e) {
            log.error("批量添加题目到题库失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量添加题目失败: " + e.getMessage(), e);
        }
        
        log.info("用户 {} 批量添加 {} 个题目到题库 {}", currentUserId, newQuestionIds.size(), questionBankId);
        return Result.success(HttpStatusEnum.OK, true);
    }
    
    /**
     * 批量从题库移除题目
     *
     * @param questionBankId 题库ID
     * @param questionIdList 题目ID列表
     * @return 移除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> adminBatchRemoveQuestionsFromBank(Long questionBankId, List<Long> questionIdList) {
        // 通用的参数校验和去重处理
        Set<Long> questionIdsSet = validateAndDeduplicateQuestionIds(questionBankId, questionIdList);
        if (questionIdsSet == null) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "题目ID列表中的题目全部不存在");
        }
        
        // 1.获取当前用户Id
        Long currentUserId = currentUserProvider.currentUserId();
        
        // 2. 删除关联记录
        LambdaQueryWrapper<QuestionBankQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
               .in(QuestionBankQuestion::getQuestionId, questionIdsSet);
        
        boolean removeResult = this.remove(wrapper);
        ThrowUtils.throwIfFalse(removeResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量移除题目失败");
        
        log.info("用户 {} 批量从题库 {} 移除 {} 个题目", currentUserId, questionBankId, questionIdsSet.size());
        return Result.success(HttpStatusEnum.OK, true);
    }
    
    /**
     * 验证参数并去重题目ID列表
     *
     * @param questionBankId 题库ID
     * @param questionIds    题目ID列表
     * @return 去重后的题目ID列表
     */
    private Set<Long> validateAndDeduplicateQuestionIds(Long questionBankId, List<Long> questionIds) {
        // 参数校验
        validateQuestionIdsFormat(questionIds);
        // 1.检查题库是否存在
        DtoValidationUtil.validateEntityExists(questionBankId, questionBankMapper::selectById, "题库");
        
        // 2.去重，避免重复处理
        List<Long> uniqueQuestionIds = questionIds.stream()
                                                  .distinct()
                                                  .toList();
        // 3.检查题目是否存在
        List<Question> existingQuestions = questionMapper.selectByIds(uniqueQuestionIds);
        Set<Long> existingQuestionIdSet = existingQuestions.stream()
                                                           .map(Question::getId)
                                                           .collect(Collectors.toSet());
        // 4.如果没有有效的题目ID，直接返回null
        if (existingQuestions.isEmpty()) {
            log.warn("题目ID列表中没有有效的题目: {}", questionIds);
            return null;
        }
        // 5.如果存在题目ID在数据库中不存在，记录日志
        if (existingQuestions.size() != uniqueQuestionIds.size()) {
            List<Long> notExistingQuestionIds = questionIds.stream()
                                                           .filter(q -> !existingQuestionIdSet.contains(q))
                                                           .toList();
            log.warn("以下题目ID在数据库中不存在: {}", notExistingQuestionIds);
        }
        return existingQuestionIdSet;
    }
    
    /**
     * 检查题目Id列表是否合法（用于批量添加和批量删除）
     *
     * @param questionIds 待检查的题目Id列表
     */
    private void validateQuestionIdsFormat(List<Long> questionIds) {
        ThrowUtils.throwIfTrue(questionIds.isEmpty(), HttpStatusEnum.BAD_REQUEST, "题目ID列表不能为空");
        ThrowUtils.throwIfTrue(questionIds.size() > 100, HttpStatusEnum.BAD_REQUEST, "批量移除题目数量不能超过100");
    }
    
}




package com.coco8talk.pm.question.service.impl;
import com.coco8talk.pm.question.service.QuestionService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.api.auth.enums.UserRoleEnums;
import com.coco8talk.pm.api.question.service.QuestionDeletionCleaner;
import com.coco8talk.pm.question.constant.QuestionConstant;
import com.coco8talk.pm.question.bank.model.dto.CreateQbqDTO;
import com.coco8talk.pm.question.bank.mapper.QuestionBankQuestionMapper;
import com.coco8talk.pm.question.bank.service.QuestionBankQuestionService;
import com.coco8talk.pm.question.convert.QuestionMapStruct;
import com.coco8talk.pm.question.convert.QuestionReviewMapStruct;
import com.coco8talk.pm.question.model.dto.CreateQuestionDTO;
import com.coco8talk.pm.question.model.dto.DeleteQuestionDTO;
import com.coco8talk.pm.question.model.dto.EditQuestionDTO;
import com.coco8talk.pm.question.model.dto.QueryQuestionDTO;
import com.coco8talk.pm.question.model.vo.QuestionForAdminVO;
import com.coco8talk.pm.question.model.vo.QuestionReviewVO;
import com.coco8talk.pm.question.model.vo.QuestionVO;
import com.coco8talk.pm.question.model.entity.Question;
import com.coco8talk.pm.question.mapper.QuestionMapper;
import com.coco8talk.pm.question.model.entity.QuestionReview;
import com.coco8talk.pm.question.mapper.QuestionReviewMapper;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.common.cache.MultiLevelCache;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.common.util.QueryWrapperUtil;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.api.user.dto.UserView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题目服务实现类
 *
 * @author coco8talk
 * @description 针对表【question(题目表)】的数据库操作Service实现
 * @createDate 2025-06-22 23:21:09
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionBankQuestionService questionBankQuestionService;
    private final List<QuestionDeletionCleaner> deletionCleaners;
    private final CurrentUserProvider currentUserProvider;
    private final QuestionReviewMapper questionReviewMapper;
    private final UserApi userApi;
    private final QuestionMapper questionMapper;
    private final QuestionBankQuestionMapper questionBankQuestionMapper;
    private final MultiLevelCache questionDetailCache;

    // ========== 构造函数 ==========

    public QuestionServiceImpl(QuestionBankQuestionService questionBankQuestionService,
                               List<QuestionDeletionCleaner> deletionCleaners,
                               CurrentUserProvider currentUserProvider,
                               QuestionReviewMapper questionReviewMapper,
                               UserApi userApi,
                               QuestionMapper questionMapper,
                               QuestionBankQuestionMapper questionBankQuestionMapper,
                               @Qualifier("questionDetailCache") MultiLevelCache questionDetailCache) {
        this.questionBankQuestionService = questionBankQuestionService;
        this.deletionCleaners = deletionCleaners;
        this.currentUserProvider = currentUserProvider;
        this.questionReviewMapper = questionReviewMapper;
        this.userApi = userApi;
        this.questionMapper = questionMapper;
        this.questionBankQuestionMapper = questionBankQuestionMapper;
        this.questionDetailCache = questionDetailCache;
    }

    /** 题目详情缓存键。RedisKeyUtil 现位于 interaction 模块内,此处按约定内联构造,避免跨模块依赖。 */
    private String detailKey(Long id) {
        return "question:detail:" + id;
    }

    // ========== 管理员接口 ==========

    /**
     * 创建题目（管理员）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> adminCreateQuestion(CreateQuestionDTO createQuestionDTO) {
        // 参数校验
        validateTitleFormat(createQuestionDTO.getTitle(), false);
        validateContentFormat(createQuestionDTO.getContent(), false);
        validateAnswerFormat(createQuestionDTO.getAnswer(), false);
        validateTagsFormat(createQuestionDTO.getTags());
        DtoValidationUtil.validateIdFormat(createQuestionDTO.getQuestionBankId(), "题库");

        // 检查题目标题唯一性
        validateQuestionUnique(createQuestionDTO.getTitle());

        // 保存题目到数据库
        Question questionToDb = QuestionMapStruct.INSTANCE.addDtoToEntity(createQuestionDTO);
        Long currentUserId = currentUserProvider.currentUserId();
        questionToDb.setCreateUserId(currentUserId);
        questionToDb.setReviewStatus(QuestionConstant.REVIEW_STATUS_APPROVED);
        boolean saveResult = this.save(questionToDb);
        ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "写入数据库失败");

        // 创建审核记录
        createQuestionReview(
            questionToDb.getId(),
            currentUserId,
            QuestionConstant.REVIEW_STATUS_APPROVED,
            "管理员创建，自动通过");

        // 建立题目与题库关联
        createQuestionBankRelation(createQuestionDTO.getQuestionBankId(), questionToDb.getId(), currentUserId);

        return Result.success(HttpStatusEnum.OK, questionToDb.getId());
    }

    /**
     * 删除题目（管理员）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> adminDeleteQuestion(DeleteQuestionDTO deleteQuestionDTO) {
        // 检查题目是否存在
        DtoValidationUtil.validateEntityExists(deleteQuestionDTO.getId(), this, "题目");

        // 删除题目
        boolean deleteResult = this.removeById(deleteQuestionDTO);
        ThrowUtils.throwIfFalse(deleteResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "删除失败");

        // 删除相关数据
        questionBankQuestionService.deleteQbqByQuestionId(deleteQuestionDTO.getId());
        deleteQuestionReviewByQuestionId(deleteQuestionDTO.getId());
        deletionCleaners.forEach(c -> c.onQuestionDeleted(deleteQuestionDTO.getId()));

        // 失效详情缓存
        questionDetailCache.evict(detailKey(deleteQuestionDTO.getId()));

        return Result.success(HttpStatusEnum.NO_CONTENT);
    }

    /**
     * 管理员批量删除题目
     *
     * @param questionIds 题目ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> adminBatchDeleteQuestion(List<Long> questionIds) {
        // 参数校验
        ThrowUtils.throwIfTrue(questionIds.isEmpty(), HttpStatusEnum.BAD_REQUEST, "题目ID列表不能为空");
        ThrowUtils.throwIfTrue(questionIds.size() > 10, HttpStatusEnum.BAD_REQUEST, "批量移除题目数量不能超过10个");
        //1.去重
        List<Long> uniqueQuestionIds = questionIds.stream()
                                                  .unordered()
                                                  .filter(id -> id != null && id >= 0)
                                                  .distinct()
                                                  .toList();
        //2.批量查询题目是否存在
        List<Long> existsQuestionIds = questionMapper.selectExistsIds(uniqueQuestionIds);
        ThrowUtils.throwIfTrue(existsQuestionIds.isEmpty(), HttpStatusEnum.BAD_REQUEST, "所有题目ID均不存在");
        if (questionIds.size() != existsQuestionIds.size()) {
            List<Long> notExistsQuestionIds =
                uniqueQuestionIds.stream()
                                 .filter(id -> !existsQuestionIds.contains(id))
                                 .toList();
            log.warn("部分题目ID不存在，无法删除，不存在的题目ID: {}", notExistsQuestionIds);
        }

        //3.批量删除题目
        boolean removeQuestionsResult = this.removeByIds(existsQuestionIds);
        ThrowUtils.throwIfFalse(removeQuestionsResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量删除题目失败");

        //4.批量删除题目与题库关联
        List<Long> existsQbqIds = questionBankQuestionMapper.listExistsQbqIdsByQuestionIds(existsQuestionIds);
        if (existsQbqIds.isEmpty()) {
            log.info("没有找到与题目关联的题库-题目关系，跳过删除关联");
        }
        int deleteResult = questionBankQuestionMapper.deleteByIds(existsQbqIds);
        ThrowUtils.throwIfTrue(deleteResult <= 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量删除题目与题库关联失败");

        //5.批量删除题目审核记录
        List<Long> existsQrIds = questionReviewMapper.listExistsReviewIdsByQuestionIds(existsQuestionIds);
        if (existsQrIds.isEmpty()) {
            log.info("没有找到与题目关联的审核记录，跳过删除审核记录");
        }
        int deleteQrResult = questionReviewMapper.deleteByIds(existsQrIds);
        ThrowUtils.throwIfTrue(deleteQrResult <= 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "批量删除题目审核记录失败");

        //6-7.批量删除题目点赞、收藏记录
        deletionCleaners.forEach(c -> c.onQuestionsDeleted(existsQuestionIds));

        //8.批量失效详情缓存
        existsQuestionIds.forEach(qid -> questionDetailCache.evict(detailKey(qid)));

        return Result.success(HttpStatusEnum.NO_CONTENT);
    }

    /**
     * 编辑题目（管理员）
     */
    @Override
    public Result<Boolean> adminEditQuestion(EditQuestionDTO editQuestionDTO) {
        // 检查题目是否存在
        DtoValidationUtil.validateEntityExists(editQuestionDTO.getId(), this, "题目");

        // 更新题目信息
        Question questionToDb = QuestionMapStruct.INSTANCE.editDtoToEntity(editQuestionDTO);
        boolean updateResult = this.updateById(questionToDb);
        ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "修改数据库失败");

        // 失效详情缓存
        questionDetailCache.evict(detailKey(editQuestionDTO.getId()));

        return Result.success(HttpStatusEnum.NO_CONTENT);
    }

    /**
     * 分页查询题目（管理员）
     */
    @Override
    public Result<Page<QuestionForAdminVO>> adminQueryQuestionPage(QueryQuestionDTO queryQuestionDTO) {
        validateQueryQuestionDTO(queryQuestionDTO);
        return queryQuestionForAdminPageWithSql(queryQuestionDTO);
    }

    // ========== 用户接口 ==========

    /**
     * 创建题目（用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createQuestion(CreateQuestionDTO createQuestionDTO) {
        validateCreateQuestionDTO(createQuestionDTO);
        validateQuestionUnique(createQuestionDTO.getTitle());

        // 保存题目
        Question questionToDb = QuestionMapStruct.INSTANCE.addDtoToEntity(createQuestionDTO);
        Long currentUserId = currentUserProvider.currentUserId();
        questionToDb.setCreateUserId(currentUserId);
        questionToDb.setReviewStatus(QuestionConstant.REVIEW_STATUS_PENDING);
        boolean saveResult = this.save(questionToDb);
        ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "写入数据库失败");

        // 创建审核记录
        createQuestionReview(questionToDb.getId(), currentUserId, QuestionConstant.REVIEW_STATUS_PENDING, "用户创建，待审核");

        // 建立题目与题库关联
        createQuestionBankRelation(createQuestionDTO.getQuestionBankId(), questionToDb.getId(), currentUserId);

        return Result.success(HttpStatusEnum.OK, questionToDb.getId());
    }

    /**
     * 删除题目（用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteQuestion(DeleteQuestionDTO deleteQuestionDTO) {
        validateDeleteQuestionDTO(deleteQuestionDTO);
        validateQuestionOwnership(deleteQuestionDTO.getId(), "删除");

        // 删除题目及相关数据
        boolean deleteResult = this.removeById(deleteQuestionDTO.getId());
        ThrowUtils.throwIfFalse(deleteResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "删除失败");

        questionBankQuestionService.deleteQbqByQuestionId(deleteQuestionDTO.getId());
        deleteQuestionReviewByQuestionId(deleteQuestionDTO.getId());
        deletionCleaners.forEach(c -> c.onQuestionDeleted(deleteQuestionDTO.getId()));

        // 失效详情缓存
        questionDetailCache.evict(detailKey(deleteQuestionDTO.getId()));

        return Result.success(HttpStatusEnum.NO_CONTENT);
    }

    /**
     * 编辑题目（用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> editQuestion(EditQuestionDTO editQuestionDTO) {
        validateEditQuestionDTO(editQuestionDTO);
        validateQuestionOwnership(editQuestionDTO.getId(), "编辑");

        // 更新题目信息
        Question questionToDb = QuestionMapStruct.INSTANCE.editDtoToEntity(editQuestionDTO);
        boolean updateResult = this.updateById(questionToDb);
        ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "修改数据库失败");

        // 更新审核状态为待审核
        updateQuestionReview(editQuestionDTO.getId());

        return Result.success(HttpStatusEnum.NO_CONTENT);
    }

    // ========== 查询接口 ==========

    /**
     * 根据ID查询题目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<QuestionVO> queryQuestionById(Long id) {
        DtoValidationUtil.validateIdNotNullFormat(id, "题目");

        QuestionVO rawVO;
        try {
            rawVO = loadApprovedDetail(id);
        } catch (UnapprovedQuestionException e) {
            return Result.fail(HttpStatusEnum.FORBIDDEN, "题目未通过审核");
        }

        if (rawVO == null) {
            return Result.fail(HttpStatusEnum.NO_CONTENT, "此题目不存在");
        }

        incrementViewCount(id);

        if (Objects.equals(QuestionConstant.NEED_VIP_YES, rawVO.getNeedVip()) && !hasVipAccess()) {
            rawVO.setAnswer("该题目答案仅VIP用户可见");
        }

        return Result.success(HttpStatusEnum.OK, rawVO);
    }

    /**
     * 经多级缓存加载"已审核通过"的原始 {@link QuestionVO}(答案完整)。
     * loader 对真正不存在的 id 返回 {@code null};对存在但未通过审核的 id 抛
     * {@link UnapprovedQuestionException}(不进缓存,保留专属文案)。
     */
    private QuestionVO loadApprovedDetail(Long id) {
        return questionDetailCache.get(detailKey(id), () -> {
            Question questionById = this.getById(id);
            if (questionById == null) {
                return null;
            }
            if (!Objects.equals(QuestionConstant.REVIEW_STATUS_APPROVED, questionById.getReviewStatus())) {
                throw new UnapprovedQuestionException();
            }
            return QuestionMapStruct.INSTANCE.entityToVo(questionById);
        }, QuestionVO.class);
    }

    /** 内部信号:题目存在但未通过审核,用于在缓存之外保留专属失败文案,不入缓存。 */
    private static final class UnapprovedQuestionException extends RuntimeException {
        UnapprovedQuestionException() {
            super(null, null, false, false);
        }
    }

    /**
     * 分页查询题目
     */
    @Override
    public Result<Page<QuestionVO>> queryQuestionPage(QueryQuestionDTO queryQuestionDTO) {
        validateQueryQuestionDTO(queryQuestionDTO);
        return queryQuestionPageWithSql(queryQuestionDTO);
    }

    // ========== 私有查询方法 ==========

    /**
     * 使用SQL查询题目（管理员）
     */
    private Result<Page<QuestionForAdminVO>> queryQuestionForAdminPageWithSql(QueryQuestionDTO queryQuestionDTO) {
        validateQueryQuestionDTO(queryQuestionDTO);

        LambdaQueryWrapper<Question> wrapper = createQueryWrapper(queryQuestionDTO);
        Page<Question> questionPage = this.page(new Page<>(queryQuestionDTO.getCurrent(), queryQuestionDTO.getPageSize()), wrapper);

        if (CollectionUtils.isEmpty(questionPage.getRecords())) {
            Page<QuestionForAdminVO> emptyPage = new Page<>(queryQuestionDTO.getCurrent(),
                queryQuestionDTO.getPageSize(), 0);
            emptyPage.setRecords(List.of());
            return Result.success(HttpStatusEnum.OK, emptyPage);
        }

        // 获取创建者信息
        List<Long> createUserIdList = questionPage.getRecords().stream()
                                                  .map(Question::getCreateUserId)
                                                  .distinct()
                                                  .collect(Collectors.toList());
        List<UserView> userVOList = userApi.getByIds(createUserIdList);

        // 获取审核信息
        List<Long> questionIdList = questionPage.getRecords().stream()
                                                .map(Question::getId)
                                                .collect(Collectors.toList());
        LambdaQueryWrapper<QuestionReview> reviewWrapper = Wrappers.lambdaQuery(QuestionReview.class)
                                                                   .in(QuestionReview::getQuestionId, questionIdList);
        List<QuestionReview> questionReviewList = questionReviewMapper.selectList(reviewWrapper);
        List<QuestionReviewVO> questionReviewVOList = QuestionReviewMapStruct.INSTANCE.entityListToVoList(questionReviewList);

        // 将用户信息转换为Map
        Map<Long, UserView> userVoMap = userVOList.stream()
                                                .collect(Collectors.toMap(UserView::id, Function.identity()));

        // 将审核信息转换为Map
        Map<Long, QuestionReviewVO> questionReviewVoMap =
            questionReviewVOList.stream()
                                .collect(Collectors.toMap(QuestionReviewVO::getQuestionId, Function.identity()));

        // 转换为VO并关联信息
        List<QuestionForAdminVO> questionVOList =
            questionPage.getRecords()
                        .stream()
                        .map(question -> {
                            QuestionForAdminVO questionForAdminVO = QuestionMapStruct.INSTANCE.entityToAdminVo(question);
                            UserView userVO = userVoMap.get(question.getCreateUserId());
                            if (userVO != null) {
                                questionForAdminVO.setCreateUser(userVO);
                            }
                            QuestionReviewVO questionReviewVO = questionReviewVoMap.get(question.getId());
                            if (questionReviewVO != null) {
                                questionForAdminVO.setReviewVo(questionReviewVO);
                            }
                            return questionForAdminVO;
                        })
                        .collect(Collectors.toList());

        Page<QuestionForAdminVO> questionForAdminVoPage = new Page<>(
            questionPage.getCurrent(),
            questionPage.getSize(),
            questionPage.getTotal());
        questionForAdminVoPage.setRecords(questionVOList);

        return Result.success(HttpStatusEnum.OK, questionForAdminVoPage);
    }

    /**
     * 使用SQL查询题目（用户）
     */
    private Result<Page<QuestionVO>> queryQuestionPageWithSql(QueryQuestionDTO queryQuestionDTO) {
        validateQueryQuestionDTO(queryQuestionDTO);

        LambdaQueryWrapper<Question> wrapper = createQueryWrapper(queryQuestionDTO);
        wrapper.eq(Question::getReviewStatus, QuestionConstant.REVIEW_STATUS_APPROVED);

        Page<Question> questionPage = this.page(new Page<>(queryQuestionDTO.getCurrent(), queryQuestionDTO.getPageSize()), wrapper);

        if (CollectionUtils.isEmpty(questionPage.getRecords())) {
            Page<QuestionVO> emptyPage = new Page<>(queryQuestionDTO.getCurrent(),
                queryQuestionDTO.getPageSize(), 0);
            emptyPage.setRecords(List.of());
            return Result.success(HttpStatusEnum.OK, emptyPage);
        }

        boolean hasVipAccess = hasVipAccess();
        List<QuestionVO> questionVoList = questionPage.getRecords().stream()
                                                      .map(question -> convertToQuestionVO(question, hasVipAccess))
                                                      .collect(Collectors.toList());

        Page<QuestionVO> questionVoPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        questionVoPage.setRecords(questionVoList);

        return Result.success(HttpStatusEnum.OK, questionVoPage);
    }

    /**
     * 创建查询条件
     */
    private LambdaQueryWrapper<Question> createQueryWrapper(QueryQuestionDTO queryQuestionDTO) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        Long questionId = queryQuestionDTO.getId();
        if (questionId != null) {
            wrapper.eq(Question::getId, questionId);
        }

        Long questionNotId = queryQuestionDTO.getNotId();
        if (questionNotId != null) {
            wrapper.ne(Question::getId, questionNotId);
        }

        String title = queryQuestionDTO.getTitle();
        if (StringUtils.isNotBlank(title)) {
            wrapper.like(Question::getTitle, title);
        }

        String content = queryQuestionDTO.getContent();
        if (StringUtils.isNotBlank(content)) {
            wrapper.like(Question::getContent, content);
        }

        String answer = queryQuestionDTO.getAnswer();
        if (StringUtils.isNotBlank(answer)) {
            wrapper.like(Question::getAnswer, answer);
        }

        String searchText = queryQuestionDTO.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            wrapper.nested(i -> i.like(Question::getTitle, searchText)
                                 .or()
                                 .like(Question::getContent, searchText)
                                 .or()
                                 .like(Question::getAnswer, searchText));
        }

        List<String> tags = queryQuestionDTO.getTags();
        if (!CollectionUtils.isEmpty(tags)) {
            for (String tag : tags) {
                wrapper.like(Question::getTags, tag);
            }
        }

        Integer difficulty = queryQuestionDTO.getDifficulty();
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }

        String sortField = queryQuestionDTO.getSortField();
        String sortOrder = queryQuestionDTO.getSortOrder();

        return QueryWrapperUtil.applySorting(wrapper, sortField, sortOrder);
    }

    // ========== 数据转换方法 ==========

    /**
     * 转换为QuestionVO并处理VIP权限
     */
    private QuestionVO convertToQuestionVO(Question question, boolean hasVipAccess) {
        QuestionVO questionVO = QuestionMapStruct.INSTANCE.entityToVo(question);
        if (isVipQuestion(question) && !hasVipAccess) {
            questionVO.setAnswer("该题目答案仅VIP用户可见");
        }
        return questionVO;
    }

    // ========== 业务逻辑辅助方法 ==========

    /**
     * 创建审核记录
     */
    private Long createQuestionReview(Long questionId, Long reviewerId, Integer reviewStatus, String reviewMessage) {
        QuestionReview questionReview = new QuestionReview();
        questionReview.setQuestionId(questionId);
        questionReview.setReviewerId(reviewerId);
        questionReview.setReviewStatus(reviewStatus);
        questionReview.setReviewMessage(reviewMessage);
        questionReview.setReviewTime(LocalDateTime.now());

        int insertResult = questionReviewMapper.insert(questionReview);
        ThrowUtils.throwIfFalse(insertResult > 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "创建审核记录失败");
        return questionReview.getId();
    }

    /**
     * 更新审核状态为待审核
     */
    private void updateQuestionReview(Long questionId) {
        LambdaQueryWrapper<QuestionReview> wrapper = Wrappers.lambdaQuery(QuestionReview.class)
                                                             .eq(QuestionReview::getQuestionId, questionId);
        QuestionReview questionReviewFromDb = questionReviewMapper.selectOne(wrapper);
        ThrowUtils.throwIfNull(questionReviewFromDb, HttpStatusEnum.INTERNAL_SERVER_ERROR, "无审核记录");

        if (questionReviewFromDb.getReviewStatus().equals(QuestionConstant.REVIEW_STATUS_PENDING)) {
            return;
        }

        // 更新题目审核状态
        Question questionToUpdate = new Question();
        questionToUpdate.setId(questionId);
        questionToUpdate.setReviewStatus(QuestionConstant.REVIEW_STATUS_PENDING);
        boolean updateQuestionResult = this.updateById(questionToUpdate);
        ThrowUtils.throwIfFalse(updateQuestionResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新题目审核状态失败");

        // 更新审核记录
        Long id = questionReviewFromDb.getId();
        QuestionReview questionReviewToDb = new QuestionReview();
        questionReviewToDb.setId(id);
        questionReviewToDb.setReviewStatus(QuestionConstant.REVIEW_STATUS_PENDING);
        int updateById = questionReviewMapper.updateById(questionReviewToDb);
        ThrowUtils.throwIfFalse(updateById > 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新审核记录失败");
    }

    /**
     * 根据题目ID删除审核记录
     */
    private void deleteQuestionReviewByQuestionId(Long questionId) {
        LambdaQueryWrapper<QuestionReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionReview::getQuestionId, questionId);
        questionReviewMapper.delete(wrapper);
    }

    /**
     * 创建题目与题库关联
     */
    private void createQuestionBankRelation(Long questionBankId, Long questionId, Long createUserId) {
        if (questionBankId != null) {
            CreateQbqDTO createQbqDTO = new CreateQbqDTO();
            createQbqDTO.setQuestionBankId(questionBankId);
            createQbqDTO.setQuestionId(questionId);
            createQbqDTO.setCreateUserId(createUserId);
            questionBankQuestionService.adminCreateQbq(createQbqDTO);
        }
    }

    /**
     * 增加浏览量
     */
    private void incrementViewCount(Long questionId) {
        questionMapper.incrementViewNum(questionId);
    }

    // ========== 参数校验方法 ==========

    private void validateCreateQuestionDTO(CreateQuestionDTO createQuestionDTO) {
        validateTitleFormat(createQuestionDTO.getTitle(), false);
        validateContentFormat(createQuestionDTO.getContent(), false);
        validateAnswerFormat(createQuestionDTO.getAnswer(), false);
        validateTagsFormat(createQuestionDTO.getTags());
        validateDifficultyFormat(createQuestionDTO.getDifficulty());
        DtoValidationUtil.validateIdFormat(createQuestionDTO.getQuestionBankId(), "题库");
    }

    private void validateEditQuestionDTO(EditQuestionDTO editQuestionDTO) {
        DtoValidationUtil.validateIdNotNullFormat(editQuestionDTO.getId(), "题目");
        validateTitleFormat(editQuestionDTO.getTitle(), true);
        validateContentFormat(editQuestionDTO.getContent(), true);
        validateAnswerFormat(editQuestionDTO.getAnswer(), true);
        validateDifficultyFormat(editQuestionDTO.getDifficulty());
        validateTagsFormat(editQuestionDTO.getTags());
    }

    private void validateDeleteQuestionDTO(DeleteQuestionDTO deleteQuestionDTO) {
        DtoValidationUtil.validateIdNotNullFormat(deleteQuestionDTO.getId(), "题目");
    }

    private void validateQueryQuestionDTO(QueryQuestionDTO queryQuestionDTO) {
        DtoValidationUtil.validateIdFormat(queryQuestionDTO.getId(), "题目");
        DtoValidationUtil.validateIdFormat(queryQuestionDTO.getNotId(), "排除题目");
        DtoValidationUtil.validateIdFormat(queryQuestionDTO.getQuestionBankId(), "题库");
        DtoValidationUtil.validateIdFormat(queryQuestionDTO.getCreateUserId(), "创建用户");
        DtoValidationUtil.validatePageCurrentFormat(queryQuestionDTO.getCurrent());
        DtoValidationUtil.validatePageSizeFormat(queryQuestionDTO.getPageSize());
        validateTitleFormat(queryQuestionDTO.getTitle(), true);
        validateContentFormat(queryQuestionDTO.getContent(), true);
        validateAnswerFormat(queryQuestionDTO.getAnswer(), true);
        validateDifficultyFormat(queryQuestionDTO.getDifficulty());
        validateSearchText(queryQuestionDTO.getSearchText());
    }

    private void validateQuestionOwnership(Long questionId, String operation) {
        Question existingQuestion = this.getById(questionId);
        ThrowUtils.throwIfNull(existingQuestion, HttpStatusEnum.BAD_REQUEST, "题目不存在");

        Long currentUserId = currentUserProvider.currentUserId();
        ThrowUtils.throwIfFalse(existingQuestion.getCreateUserId().equals(currentUserId),
            HttpStatusEnum.FORBIDDEN,
            "只能" + operation + "自己创建的题目");
    }

    private void validateQuestionUnique(String title) {
        ThrowUtils.throwIfBlank(title, HttpStatusEnum.BAD_REQUEST, "题目标题不能为空");

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getTitle, title);
        Question questionFromDb = this.getOne(wrapper);
        ThrowUtils.throwIfNotNull(questionFromDb, HttpStatusEnum.BAD_REQUEST, "该题目标题已存在，请更换后重试");
    }

    private void validateTitleFormat(String title, boolean nullable) {
        if (nullable) {
            ThrowUtils.throwIfTrue(StringUtils.isNotBlank(title) && title.length() > QuestionConstant.TITLE_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST, "题目标题不能超过" + QuestionConstant.TITLE_MAX_LENGTH + "个字符");
        } else {
            ThrowUtils.throwIfFalse(StringUtils.isNotBlank(title) && title.length() <= QuestionConstant.TITLE_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST, "题目标题不能为空且不能超过" + QuestionConstant.TITLE_MAX_LENGTH + "个字符");
        }
    }

    private void validateContentFormat(String content, boolean nullable) {
        if (nullable) {
            ThrowUtils.throwIfTrue(StringUtils.isNotBlank(content) && content.length() > QuestionConstant.CONTENT_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST, "题目内容不能超过" + QuestionConstant.CONTENT_MAX_LENGTH + "个字符");
        } else {
            ThrowUtils.throwIfFalse(StringUtils.isNotBlank(content) && content.length() <= QuestionConstant.CONTENT_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST, "题目内容不能为空且不能超过" + QuestionConstant.CONTENT_MAX_LENGTH + "个字符");
        }
    }

    private void validateAnswerFormat(String answer, boolean nullable) {
        if (nullable) {
            ThrowUtils.throwIfTrue(StringUtils.isNotBlank(answer) && answer.length() > QuestionConstant.ANSWER_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST, "题目答案不能超过" + QuestionConstant.ANSWER_MAX_LENGTH + "个字符");
        } else {
            ThrowUtils.throwIfFalse(StringUtils.isNotBlank(answer) && answer.length() <= QuestionConstant.ANSWER_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST, "题目答案不能为空且不能超过" + QuestionConstant.ANSWER_MAX_LENGTH + "个字符");
        }
    }

    private void validateTagsFormat(List<String> tags) {
        ThrowUtils.throwIfTrue(!CollectionUtils.isEmpty(tags) && tags.size() > QuestionConstant.TAGS_MAX_COUNT,
            HttpStatusEnum.BAD_REQUEST, "题目标签不能超过" + QuestionConstant.TAGS_MAX_COUNT + "个");
    }

    private void validateSearchText(String searchText) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(searchText) && searchText.length() > QuestionConstant.SEARCH_TEXT_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "搜索关键词不能超过" + QuestionConstant.SEARCH_TEXT_MAX_LENGTH + "个字符");
    }

    private void validateDifficultyFormat(Integer difficulty) {
        ThrowUtils.throwIfTrue(difficulty != null && (difficulty < QuestionConstant.DIFFICULTY_MIN_VALUE || difficulty > QuestionConstant.DIFFICULTY_MAX_VALUE),
            HttpStatusEnum.BAD_REQUEST, "题目难度必须在" + QuestionConstant.DIFFICULTY_MIN_VALUE + "-" + QuestionConstant.DIFFICULTY_MAX_VALUE + "之间");
    }

    // ========== 工具方法 ==========

    private boolean isVipQuestion(Question question) {
        return question.getNeedVip() != null && question.getNeedVip().equals(QuestionConstant.NEED_VIP_YES);
    }

    private boolean hasVipAccess() {
        if (!currentUserProvider.isLoggedIn()) {
            return false;
        }

        Integer userRole = currentUserProvider.currentUserRole().getCode();
        return userRole != null && (userRole.equals(UserRoleEnums.VIP.getCode()) || userRole.equals(UserRoleEnums.ADMIN.getCode()));
    }
}

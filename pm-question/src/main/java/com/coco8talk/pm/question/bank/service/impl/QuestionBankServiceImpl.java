package com.coco8talk.pm.question.bank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.question.bank.mapper.QuestionBankMapper;
import com.coco8talk.pm.question.bank.mapper.QuestionBankQuestionMapper;
import com.coco8talk.pm.question.bank.convert.QuestionBankMapStruct;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.question.bank.model.dto.AddQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.DeleteQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.EditQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.QueryQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.entity.QuestionBank;
import com.coco8talk.pm.question.bank.model.entity.QuestionBankQuestion;
import com.coco8talk.pm.api.question.dto.QuestionForBankVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankDetailVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankForAdminVO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankVO;
import com.coco8talk.pm.question.bank.service.QuestionBankQuestionService;
import com.coco8talk.pm.question.bank.service.QuestionBankService;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.common.util.QueryWrapperUtil;
import com.coco8talk.pm.common.cache.MultiLevelCache;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题库服务实现类
 *
 * @author coco8talk
 * @description 针对表【question_bank(题库表)】的数据库操作Service实现
 * @createDate 2025-06-22 23:21:13
 */
@Slf4j
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
    implements QuestionBankService {

    private final UserApi userApi;
    private final QuestionBankQuestionService questionBankQuestionService;
    private final CurrentUserProvider currentUserProvider;
    private final QuestionBankQuestionMapper questionBankQuestionMapper;
    private final MultiLevelCache questionBankDetailCache;
    private final DistributedLock distributedLock;

    /** 题库标题唯一性锁租约:5 秒,覆盖一次"查重 + 写入"临界区。 */
    private static final long QBANK_TITLE_LOCK_LEASE_MILLIS = 5_000L;

    public QuestionBankServiceImpl(UserApi userApi,
                                   QuestionBankQuestionService questionBankQuestionService,
                                   CurrentUserProvider currentUserProvider,
                                   QuestionBankQuestionMapper questionBankQuestionMapper,
                                   @Qualifier("questionBankDetailCache") MultiLevelCache questionBankDetailCache,
                                   DistributedLock distributedLock) {
        this.userApi = userApi;
        this.questionBankQuestionService = questionBankQuestionService;
        this.currentUserProvider = currentUserProvider;
        this.questionBankQuestionMapper = questionBankQuestionMapper;
        this.questionBankDetailCache = questionBankDetailCache;
        this.distributedLock = distributedLock;
    }

    /** 题库详情缓存键。RedisKeyUtil 现位于 interaction 模块内,此处按约定内联构造,避免跨模块依赖。 */
    private String detailKey(Long id) {
        return "questionbank:detail:" + id;
    }
    
    // ========== 管理员接口 ==========
    
    /**
     * 创建题库（管理员）
     */
    @Override
    public Result<Long> adminCreateQuestionBank(AddQuestionBankDTO addQuestionBankDTO) {
        // 参数校验
        validateAddQuestionBankDTO(addQuestionBankDTO);

        String title = addQuestionBankDTO.getTitle();
        Long currentUserId = currentUserProvider.currentUserId();

        // 分布式锁串行化"同名题库"的并发创建:锁住"标题查重 + 写入"临界区,避免并发下重复同名题库
        String lockKey = "lock:qbank:title:" + title;
        return distributedLock.runExclusive(lockKey, 0, QBANK_TITLE_LOCK_LEASE_MILLIS, () -> {
            validateQuestionBankTitleUnique(title);

            // 创建题库
            QuestionBank questionBankToDb = QuestionBankMapStruct.INSTANCE
                .addQuestionBankDtoToQuestionBank(addQuestionBankDTO);
            questionBankToDb.setCreateUserId(currentUserId);

            boolean saveResult = this.save(questionBankToDb);
            ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "写入数据库失败");

            return Result.success(HttpStatusEnum.OK, questionBankToDb.getId());
        });
    }
    
    /**
     * 删除题库（管理员）
     */
    @Override
    public Result<Boolean> adminDeleteQuestionBank(DeleteQuestionBankDTO deleteQuestionDTO) {
        // 检查题库是否存在
        QuestionBank questionBankById = DtoValidationUtil.validateEntityExists(deleteQuestionDTO.getId(), this, "题库");
        
        // 检查题库中是否有题目
        LambdaQueryWrapper<QuestionBankQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankById.getId());
        List<QuestionBankQuestion> questionExistsNum = questionBankQuestionMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(questionExistsNum)) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "题库中有题目不允许删除题库");
        }
        
        // 删除题库
        boolean deleteQuestionBankResult = this.removeById(questionBankById);
        ThrowUtils.throwIfFalse(deleteQuestionBankResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "删除题库失败");

        // 失效详情缓存
        questionBankDetailCache.evict(detailKey(questionBankById.getId()));

        return Result.success(HttpStatusEnum.OK, true);
    }
    
    /**
     * 编辑题库（管理员）
     */
    @Override
    public Result<Boolean> adminEditQuestionBank(EditQuestionBankDTO editQuestionBankDTO) {
        // 参数校验
        validateEditQuestionBankDTO(editQuestionBankDTO);
        DtoValidationUtil.validateEntityExists(editQuestionBankDTO.getId(), this, "题库");
        
        // 更新题库
        QuestionBank questionBankToDb = QuestionBankMapStruct.INSTANCE.editDtoToEntity(editQuestionBankDTO);
        questionBankToDb.setEditTime(LocalDateTime.now());
        boolean updateResult = this.updateById(questionBankToDb);
        ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新失败");

        // 失效详情缓存
        questionBankDetailCache.evict(detailKey(editQuestionBankDTO.getId()));

        return Result.success(HttpStatusEnum.OK, true);
    }
    
    // ========== 查询接口 ==========
    
    /**
     * 根据ID查询题库
     */
    @Override
    public Result<QuestionBankDetailVO> queryQuestionBankById(Long questionBankId, Boolean needQueryQuestionList) {
        // 参数校验
        DtoValidationUtil.validateIdFormat(questionBankId, "题库");

        // 经多级缓存加载题库"基础详情"(含创建者,不含题目列表)
        QuestionBankDetailVO baseVO = loadBankBaseDetail(questionBankId);
        if (baseVO == null) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "题库不存在");
        }

        // 题目列表是每请求、可变的(分页 + 成员关系会变),不入缓存,
        // 按需在(新解码/拷贝出的)VO 上填充
        if (needQueryQuestionList != null && needQueryQuestionList) {
            fillQuestionList(baseVO, questionBankId);
        }

        return Result.success(HttpStatusEnum.OK, baseVO);
    }

    /**
     * 经多级缓存加载题库基础详情(含创建者信息,不含题目列表)。
     * loader 对不存在的 id 返回 {@code null}(缓存空值哨兵防穿透)。
     */
    private QuestionBankDetailVO loadBankBaseDetail(Long questionBankId) {
        return questionBankDetailCache.get(detailKey(questionBankId), () -> {
            QuestionBank questionBankById = this.getById(questionBankId);
            if (questionBankById == null) {
                return null; // 不存在:缓存空值哨兵防穿透
            }
            return convertToBaseVo(questionBankById);
        }, QuestionBankDetailVO.class);
    }

    /**
     * 分页查询题库（管理员）
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public Result<Page<QuestionBankForAdminVO>> adminQueryPageQuestionBank(QueryQuestionBankDTO queryQuestionBankDTO) {
        // 参数校验
        validateQueryQuestionBankDTO(queryQuestionBankDTO);
        
        // 创建查询条件
        LambdaQueryWrapper<QuestionBank> wrapper = createQueryPageQuestionBankWrapper(queryQuestionBankDTO);
        Page<QuestionBank> questionBankPage = this.page(
            new Page<>(queryQuestionBankDTO.getCurrent(), queryQuestionBankDTO.getPageSize()),
            wrapper
        );
        List<QuestionBank> qbFromDb = questionBankPage.getRecords();
        if (CollectionUtils.isEmpty(qbFromDb)) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "根据当前条件未查询出题库");
        }
        
        // 批量获取创建用户信息
        List<Long> createUserIds = qbFromDb.stream()
            .map(QuestionBank::getCreateUserId)
            .distinct()
            .toList();
        List<UserView> userViewList = userApi.getByIds(createUserIds);
        // 将用户信息转换为Map，提供O(1)查找性能
        Map<Long, UserView> userViewMap = userViewList.stream()
            .collect(Collectors.toMap(UserView::id, Function.identity()));

        // 转换为VO并填充创建用户信息
        List<QuestionBankForAdminVO> questionBankForAdminVOList = qbFromDb.stream()
            .map(questionBank -> {
                QuestionBankForAdminVO adminVO = QuestionBankMapStruct.INSTANCE.entityToForAdminVo(questionBank);
                // 使用Map进行O(1)查找设置创建者信息 [[memory:3097413]]
                UserView userView = userViewMap.get(questionBank.getCreateUserId());
                if (userView != null) {
                    adminVO.setCreateUser(userView);
                }
                return adminVO;
            })
            .toList();
        
        // 构建分页结果
        Page<QuestionBankForAdminVO> questionBankForAdminVoPage = new Page<>(
            questionBankPage.getCurrent(),
            questionBankPage.getSize(),
            questionBankPage.getTotal()
        );
        questionBankForAdminVoPage.setRecords(questionBankForAdminVOList);
        
        return Result.success(HttpStatusEnum.OK, questionBankForAdminVoPage);
    }
    
    /**
     * 分页查询题库（用户）
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public Result<Page<QuestionBankVO>> queryPageQuestionBank(QueryQuestionBankDTO queryQuestionBankDTO) {
        // 参数校验
        validateQueryQuestionBankDTO(queryQuestionBankDTO);
        
        // 创建查询条件
        LambdaQueryWrapper<QuestionBank> wrapper = createQueryPageQuestionBankWrapper(queryQuestionBankDTO);
        Page<QuestionBank> questionBankPage = this.page(new Page<>(queryQuestionBankDTO.getCurrent(), queryQuestionBankDTO.getPageSize()), wrapper);
        List<QuestionBank> qbFromDb = questionBankPage.getRecords();
        if (CollectionUtils.isEmpty(qbFromDb)) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "根据当前条件未查询出题库");
        }
        
        // 转换为VO
        Page<QuestionBankVO> questionBankVoPage = QuestionBankMapStruct.INSTANCE.entityPageToVoPage(questionBankPage);
        return Result.success(HttpStatusEnum.OK, questionBankVoPage);
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 验证创建题库参数
     */
    private void validateAddQuestionBankDTO(AddQuestionBankDTO addQuestionBankDTO) {
        validateTitleFormat(addQuestionBankDTO.getTitle(), false);
        validateDescriptionFormat(addQuestionBankDTO.getDescription(), false);
        validatePictureFormat(addQuestionBankDTO.getPicture());
    }
    
    /**
     * 验证编辑题库参数
     */
    private void validateEditQuestionBankDTO(EditQuestionBankDTO editQuestionBankDTO) {
        DtoValidationUtil.validateIdNotNullFormat(editQuestionBankDTO.getId(), "题库");
        validateTitleFormat(editQuestionBankDTO.getTitle(), true);
        validateDescriptionFormat(editQuestionBankDTO.getDescription(), true);
        validatePictureFormat(editQuestionBankDTO.getPicture());
    }
    
    /**
     * 验证查询题库参数
     */
    private void validateQueryQuestionBankDTO(QueryQuestionBankDTO queryQuestionBankDTO) {
        DtoValidationUtil.validateIdFormat(queryQuestionBankDTO.getId(), "题库");
        DtoValidationUtil.validateIdFormat(queryQuestionBankDTO.getUserId(), "创建用户");
        DtoValidationUtil.validatePageCurrentFormat(queryQuestionBankDTO.getCurrent());
        DtoValidationUtil.validatePageSizeFormat(queryQuestionBankDTO.getPageSize());
        validateTitleFormat(queryQuestionBankDTO.getTitle(), true);
        validateDescriptionFormat(queryQuestionBankDTO.getDescription(), true);
        validateSearchTextFormat(queryQuestionBankDTO.getSearchText());
    }
    
    /**
     * 验证题库标题格式
     */
    private void validateTitleFormat(String title, boolean nullable) {
        if (nullable) {
            ThrowUtils.throwIfTrue(StringUtils.isNotBlank(title) && title.length() > 80,
                HttpStatusEnum.BAD_REQUEST, "题库标题不能超过80个字符");
        } else {
            ThrowUtils.throwIfFalse(StringUtils.isNotBlank(title) && title.length() <= 80,
                HttpStatusEnum.BAD_REQUEST, "题库标题不能为空且不能超过80个字符");
        }
    }
    
    /**
     * 验证题库描述格式
     */
    private void validateDescriptionFormat(String description, boolean nullable) {
        if (nullable) {
            ThrowUtils.throwIfTrue(StringUtils.isNotBlank(description) && description.length() > 200,
                HttpStatusEnum.BAD_REQUEST, "题库描述不能超过200个字符");
        } else {
            ThrowUtils.throwIfFalse(StringUtils.isNotBlank(description) && description.length() <= 200,
                HttpStatusEnum.BAD_REQUEST, "题库描述不能为空且不能超过200个字符");
        }
    }
    
    /**
     * 验证题库图片格式
     */
    private void validatePictureFormat(String picture) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(picture) && picture.length() > 1024,
            HttpStatusEnum.BAD_REQUEST, "题库图片URL不能超过1024个字符");
    }
    
    /**
     * 验证搜索关键词格式
     */
    private void validateSearchTextFormat(String searchText) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(searchText) && searchText.length() > 50,
            HttpStatusEnum.BAD_REQUEST, "搜索关键词不能超过50个字符");
    }
    
    /**
     * 验证题库标题唯一性
     */
    private void validateQuestionBankTitleUnique(String title) {
        ThrowUtils.throwIfBlank(title, HttpStatusEnum.BAD_REQUEST, "题库标题不能为空");
        
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getTitle, title);
        QuestionBank questionBankFromDb = this.getOne(wrapper);
        ThrowUtils.throwIfNotNull(questionBankFromDb, HttpStatusEnum.BAD_REQUEST, "该题库标题已存在，请更换后重试");
    }
    
    /**
     * 创建查询条件
     */
    private LambdaQueryWrapper<QuestionBank> createQueryPageQuestionBankWrapper(QueryQuestionBankDTO queryQuestionBankDTO) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        
        // 根据题库ID查询
        Long questionBankId = queryQuestionBankDTO.getId();
        if (questionBankId != null) {
            wrapper.eq(QuestionBank::getId, questionBankId);
        }
        
        // 根据题库标题查询
        String title = queryQuestionBankDTO.getTitle();
        if (StringUtils.isNotBlank(title)) {
            wrapper.like(QuestionBank::getTitle, title);
        }
        
        // 根据题库描述查询
        String description = queryQuestionBankDTO.getDescription();
        if (StringUtils.isNotBlank(description)) {
            wrapper.like(QuestionBank::getDescription, description);
        }
        
        // 根据搜索关键词查询
        String searchText = queryQuestionBankDTO.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            wrapper.nested(i -> i.like(QuestionBank::getTitle, searchText)
                                 .or()
                                 .like(QuestionBank::getDescription, searchText));
        }
        
        // 根据创建用户ID查询
        Long createUserId = queryQuestionBankDTO.getUserId();
        if (createUserId != null) {
            wrapper.eq(QuestionBank::getCreateUserId, createUserId);
        }
        
        // 应用排序条件
        return QueryWrapperUtil.applySorting(wrapper, queryQuestionBankDTO.getSortField(), queryQuestionBankDTO.getSortOrder());
    }
    
    /**
     * 转换为基础详情 VO(含创建者信息),不含题目列表 —— 题目列表为每请求按需填充、不入缓存。
     */
    private QuestionBankDetailVO convertToBaseVo(QuestionBank questionBank) {
        QuestionBankDetailVO questionBankDetailVO = QuestionBankMapStruct.INSTANCE.entityToVo(questionBank);

        // 填充创建用户信息
        Long createUserId = questionBank.getCreateUserId();
        if (!ObjectUtils.isEmpty(createUserId)) {
            UserView userView = userApi.getById(createUserId);
            if (userView != null) {
                questionBankDetailVO.setUserVO(userView);
            }
        }

        return questionBankDetailVO;
    }

    /**
     * 按需填充题目分页列表(每请求,不入缓存)。
     */
    private void fillQuestionList(QuestionBankDetailVO vo, Long questionBankId) {
        Result<Page<QuestionForBankVO>> questionsResult =
            questionBankQuestionService.getQuestionsForBankById(questionBankId, 1, 10);
        if (questionsResult.getCode() == HttpStatusEnum.OK.getCode() && questionsResult.getData() != null) {
            vo.setQuestionPageVO(questionsResult.getData());
        }
    }
}





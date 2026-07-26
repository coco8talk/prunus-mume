package com.coco8talk.pm.question.service.impl;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.CommonConstant;
import com.coco8talk.pm.question.constant.QuestionReviewConstant;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.question.mapper.QuestionMapper;
import com.coco8talk.pm.question.mapper.QuestionReviewMapper;
import com.coco8talk.pm.question.convert.QuestionReviewMapStruct;
import com.coco8talk.pm.question.model.dto.QueryQuestionReviewDTO;
import com.coco8talk.pm.question.model.dto.ReviewQuestionDTO;
import com.coco8talk.pm.question.model.entity.Question;
import com.coco8talk.pm.question.model.entity.QuestionReview;
import com.coco8talk.pm.api.user.dto.UserView;
import com.coco8talk.pm.question.model.vo.QuestionReviewVO;
import com.coco8talk.pm.question.model.vo.QuestionReviewWithDetailVO;
import com.coco8talk.pm.question.service.QuestionReviewService;
import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.common.util.QueryWrapperUtil;
import com.coco8talk.pm.common.cache.MultiLevelCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目审核service层实现类
 *
 * @author coco8talk
 * @description 针对表【question_review(题目审核表)】的数据库操作Service实现
 * @createDate 2025-07-05 19:58:33
 */
@Service
public class QuestionReviewServiceImpl extends ServiceImpl<QuestionReviewMapper, QuestionReview>
        implements QuestionReviewService {
    
    private final UserApi userApi;
    private final CurrentUserProvider currentUserProvider;
    private final QuestionMapper questionMapper;
    private final MultiLevelCache questionDetailCache;

    public QuestionReviewServiceImpl(UserApi userApi,
                                     CurrentUserProvider currentUserProvider,
                                     QuestionMapper questionMapper,
                                     @Qualifier("questionDetailCache") MultiLevelCache questionDetailCache) {
        this.userApi = userApi;
        this.currentUserProvider = currentUserProvider;
        this.questionMapper = questionMapper;
        this.questionDetailCache = questionDetailCache;
    }
    
    /**
     * 审核题目（管理员）
     *
     * @param reviewQuestionDTO 审核信息
     * @return 审核结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> reviewQuestion(ReviewQuestionDTO reviewQuestionDTO) {
        // 参数校验（并检查题目是否存在）
        validateReviewQuestionDTO(reviewQuestionDTO);
        
        // 检查是否已有审核记录
        LambdaQueryWrapper<QuestionReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionReview::getQuestionId, reviewQuestionDTO.getQuestionId())
                .orderByDesc(QuestionReview::getReviewTime)
                .last("LIMIT 1");
        QuestionReview existingReview = this.getOne(wrapper);
        
        // 如果已经有相同状态的审核记录，不允许重复审核
        if (existingReview != null && existingReview.getReviewStatus().equals(reviewQuestionDTO.getReviewStatus())) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "该题目已经是此审核状态");
        }
        
        // 获取当前审核人
        Long currentUserId = currentUserProvider.currentUserId();
        
        // 同时更新question表的review_status字段
        Question questionToUpdate = new Question();
        questionToUpdate.setId(reviewQuestionDTO.getQuestionId());
        questionToUpdate.setReviewStatus(reviewQuestionDTO.getReviewStatus());
        boolean updateQuestionResult = questionMapper.updateById(questionToUpdate) > 0;
        ThrowUtils.throwIfFalse(updateQuestionResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新题目审核状态失败");

        // 审核状态变更后失效详情缓存:已缓存的"已通过"VO 若被改判驳回,必须清掉避免读到陈旧数据
        questionDetailCache.evict("question:detail:" + reviewQuestionDTO.getQuestionId());
        
        // 更新question_review表的审核记录
        LambdaUpdateWrapper<QuestionReview> updateWrapper = Wrappers.lambdaUpdate(QuestionReview.class)
                .eq(QuestionReview::getQuestionId, reviewQuestionDTO.getQuestionId())
                .set(QuestionReview::getReviewerId, currentUserId)
                .set(QuestionReview::getReviewStatus, reviewQuestionDTO.getReviewStatus())
                .set(QuestionReview::getReviewMessage, reviewQuestionDTO.getReviewMessage());
        
        boolean updateResult = this.update(updateWrapper);
        ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新审核记录失败");
        
        return Result.success(HttpStatusEnum.OK, true);
    }
    
    /**
     * 根据题目ID查询审核记录
     *
     * @param questionId 题目ID
     * @return 审核记录
     */
    @Override
    public Result<QuestionReviewVO> getReviewByQuestionId(Long questionId) {
        // 参数校验
        DtoValidationUtil.validateIdFormat(questionId, "题目");
        
        // 查询最新审核记录
        LambdaQueryWrapper<QuestionReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionReview::getQuestionId, questionId)
                .orderByDesc(QuestionReview::getReviewTime)
                .last("LIMIT 1");
        
        QuestionReview questionReview = this.getOne(wrapper);
        if (questionReview == null) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "该题目暂无审核记录");
        }
        
        // 转换为VO并填充额外信息
        QuestionReviewVO reviewVO = QuestionReviewMapStruct.INSTANCE.entityToVo(questionReview);
        fillReviewVoExtraInfo(List.of(reviewVO));
        
        return Result.success(HttpStatusEnum.OK, reviewVO);
    }
    
    /**
     * 分页查询审核记录（管理员）
     *
     * @param queryQuestionReviewDTO 查询条件
     * @return 审核记录分页
     */
    @Override
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Page<QuestionReviewVO>> adminQueryReviewPage(QueryQuestionReviewDTO queryQuestionReviewDTO) {
        // 参数校验
        validateQueryQuestionReviewDTO(queryQuestionReviewDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<QuestionReview> wrapper = createQueryWrapper(queryQuestionReviewDTO);
        
        // 执行分页查询
        Page<QuestionReview> reviewPage = this.page(
                new Page<>(queryQuestionReviewDTO.getCurrent(), queryQuestionReviewDTO.getPageSize()),
                wrapper
        );
        
        // 转换为VO并填充额外信息
        Page<QuestionReviewVO> voPage = QuestionReviewMapStruct.INSTANCE.entityPageToVoPage(reviewPage);
        fillReviewVoExtraInfo(voPage.getRecords());
        
        return Result.success(HttpStatusEnum.OK, voPage);
    }
    
    /**
     * 获取待审核题目数量
     *
     * @return 待审核题目数量
     */
    @Override
    public Result<Long> getPendingReviewCount() {
        // 直接查询question表中review_status为待审核的题目数量
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getReviewStatus, QuestionReviewConstant.REVIEW_STATUS_PENDING);
        
        long count = questionMapper.selectCount(wrapper);
        return Result.success(HttpStatusEnum.OK, count);
    }
    
    /**
     * 获取待审核题目列表（包含题目详细信息）
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 待审核题目分页列表
     */
    @Override
    public Result<Page<QuestionReviewWithDetailVO>> getPendingReviewList(Integer current, Integer pageSize) {
        // 参数校验
        DtoValidationUtil.validatePageCurrentFormat(current);
        DtoValidationUtil.validatePageSizeFormat(pageSize);
        
        // 查询待审核的审核记录
        LambdaQueryWrapper<QuestionReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionReview::getReviewStatus, QuestionReviewConstant.REVIEW_STATUS_PENDING)
                .orderByDesc(QuestionReview::getReviewTime);
        
        Page<QuestionReview> reviewPage = this.page(new Page<>(current, pageSize), wrapper);
        
        // 转换为带详细信息的VO
        Page<QuestionReviewWithDetailVO> voPage = new Page<>();
        voPage.setCurrent(reviewPage.getCurrent());
        voPage.setSize(reviewPage.getSize());
        voPage.setTotal(reviewPage.getTotal());
        
        List<QuestionReviewWithDetailVO> voList = reviewPage.getRecords().stream()
                .map(this::convertToQuestionReviewWithDetailVO)
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        
        return Result.success(HttpStatusEnum.OK, voPage);
    }
    
    /**
     * 转换为包含题目详细信息的VO
     *
     * @param questionReview 审核记录
     * @return 包含题目详细信息的VO
     */
    private QuestionReviewWithDetailVO convertToQuestionReviewWithDetailVO(QuestionReview questionReview) {
        // 使用MapStruct进行基础映射
        QuestionReviewWithDetailVO vo = QuestionReviewMapStruct.INSTANCE.reviewToReviewWithDetailVO(questionReview);
        
        // 设置审核状态描述
        vo.setReviewStatusDesc(getReviewStatusDesc(questionReview.getReviewStatus()));
        
        // 获取并设置题目详细信息
        if (questionReview.getQuestionId() != null) {
            Question question = questionMapper.selectById(questionReview.getQuestionId());
            if (question != null) {
                vo.setQuestionTitle(question.getTitle());
                vo.setQuestionContent(question.getContent());
                vo.setQuestionAnswer(question.getAnswer());
                vo.setQuestionTags(question.getTags());
                vo.setQuestionDifficulty(question.getDifficulty());
                vo.setQuestionCreateTime(question.getCreateTime());
            }
        }
        
        // 获取并设置审核人信息
        if (questionReview.getReviewerId() != null) {
            UserView reviewer = userApi.getById(questionReview.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.userName());
            }
        }
        
        return vo;
    }
    
    /**
     * 创建查询条件
     *
     * @param queryQuestionReviewDTO 查询条件
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<QuestionReview> createQueryWrapper(QueryQuestionReviewDTO queryQuestionReviewDTO) {
        LambdaQueryWrapper<QuestionReview> wrapper = new LambdaQueryWrapper<>();
        
        // 审核记录ID
        if (queryQuestionReviewDTO.getId() != null) {
            wrapper.eq(QuestionReview::getId, queryQuestionReviewDTO.getId());
        }
        // 题目ID
        if (queryQuestionReviewDTO.getQuestionId() != null) {
            wrapper.eq(QuestionReview::getQuestionId, queryQuestionReviewDTO.getQuestionId());
        }
        // 审核人ID
        if (queryQuestionReviewDTO.getReviewerId() != null) {
            wrapper.eq(QuestionReview::getReviewerId, queryQuestionReviewDTO.getReviewerId());
        }
        // 审核状态
        if (queryQuestionReviewDTO.getReviewStatus() != null) {
            wrapper.eq(QuestionReview::getReviewStatus, queryQuestionReviewDTO.getReviewStatus());
        }
        // 审核时间范围
        if (queryQuestionReviewDTO.getReviewStartTime() != null) {
            wrapper.ge(QuestionReview::getReviewTime, queryQuestionReviewDTO.getReviewStartTime());
        }
        if (queryQuestionReviewDTO.getReviewEndTime() != null) {
            wrapper.le(QuestionReview::getReviewTime, queryQuestionReviewDTO.getReviewEndTime());
        }
        // 排序
        String sortField = queryQuestionReviewDTO.getSortField();
        String sortOrder = queryQuestionReviewDTO.getSortOrder();
        if (StringUtils.isNoneBlank(sortField, sortOrder)) {
            QueryWrapperUtil.validateSortFieldSecurity(sortField);
            wrapper.last("ORDER BY " + QueryWrapperUtil.camelToUnderLine(sortField) + " " +
                    (CommonConstant.ASC.equals(sortOrder) ? "ASC" : "DESC"));
        } else {
            // 默认按审核时间倒序
            wrapper.orderByDesc(QuestionReview::getReviewTime);
        }
        
        return wrapper;
    }
    
    /**
     * 填充审核VO的额外信息
     *
     * @param reviewVOList 审核VO列表
     */
    private void fillReviewVoExtraInfo(List<QuestionReviewVO> reviewVOList) {
        if (reviewVOList == null || reviewVOList.isEmpty()) {
            return;
        }
        
        // 批量获取题目信息
        Set<Long> questionIds = reviewVOList.stream()
                .map(QuestionReviewVO::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Long, Question> questionMap = Collections.emptyMap();
        if (!questionIds.isEmpty()) {
            List<Question> questions = questionMapper.selectByIds(questionIds);
            questionMap = questions.stream()
                    .collect(Collectors.toMap(Question::getId, question -> question));
        }
        
        // 批量获取审核人信息
        Set<Long> reviewerIds = reviewVOList.stream()
                .map(QuestionReviewVO::getReviewerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Long, UserView> reviewerMap = Collections.emptyMap();
        if (!reviewerIds.isEmpty()) {
            List<UserView> reviewers = userApi.getByIds(reviewerIds);
            reviewerMap = reviewers.stream()
                    .collect(Collectors.toMap(UserView::id, user -> user));
        }
        
        // 填充额外信息
        for (QuestionReviewVO reviewVO : reviewVOList) {
            // 填充题目标题
            if (reviewVO.getQuestionId() != null) {
                Question question = questionMap.get(reviewVO.getQuestionId());
                if (question != null) {
                    reviewVO.setQuestionTitle(question.getTitle());
                }
            }
            
            // 填充审核人名称
            if (reviewVO.getReviewerId() != null) {
                UserView reviewer = reviewerMap.get(reviewVO.getReviewerId());
                if (reviewer != null) {
                    reviewVO.setReviewerName(reviewer.userName());
                }
            }
            
            // 填充审核状态描述
            if (reviewVO.getReviewStatus() != null) {
                reviewVO.setReviewStatusDesc(getReviewStatusDesc(reviewVO.getReviewStatus()));
            }
        }
    }
    
    /**
     * 获取审核状态描述
     *
     * @param reviewStatus 审核状态
     * @return 状态描述
     */
    private String getReviewStatusDesc(Integer reviewStatus) {
        return switch (reviewStatus) {
            case QuestionReviewConstant.REVIEW_STATUS_PENDING -> QuestionReviewConstant.REVIEW_STATUS_PENDING_DESC;
            case QuestionReviewConstant.REVIEW_STATUS_APPROVED -> QuestionReviewConstant.REVIEW_STATUS_APPROVED_DESC;
            case QuestionReviewConstant.REVIEW_STATUS_REJECTED -> QuestionReviewConstant.REVIEW_STATUS_REJECTED_DESC;
            default -> "未知状态";
        };
    }
    
    // ========== 参数校验方法 ==========
    
    /**
     * 验证审核题目DTO
     *
     * @param reviewQuestionDTO 审核题目DTO
     */
    private void validateReviewQuestionDTO(ReviewQuestionDTO reviewQuestionDTO) {
        DtoValidationUtil.validateEntityExists(reviewQuestionDTO.getQuestionId(), questionMapper::selectById, "题目");
        validateReviewStatus(reviewQuestionDTO.getReviewStatus());
        validateReviewMessage(reviewQuestionDTO.getReviewMessage());
    }
    
    /**
     * 验证查询审核记录DTO
     *
     * @param queryQuestionReviewDTO 查询审核记录DTO
     */
    private void validateQueryQuestionReviewDTO(QueryQuestionReviewDTO queryQuestionReviewDTO) {
        DtoValidationUtil.validateIdFormat(queryQuestionReviewDTO.getId(), "审核记录");
        DtoValidationUtil.validateIdFormat(queryQuestionReviewDTO.getQuestionId(), "题目");
        DtoValidationUtil.validateIdFormat(queryQuestionReviewDTO.getReviewerId(), "审核记录");
        DtoValidationUtil.validatePageCurrentFormat(queryQuestionReviewDTO.getCurrent());
        DtoValidationUtil.validatePageSizeFormat(queryQuestionReviewDTO.getPageSize());
        validateReviewStatus(queryQuestionReviewDTO.getReviewStatus());
    }
    
    /**
     * 验证审核状态
     *
     * @param reviewStatus 审核状态
     */
    private void validateReviewStatus(Integer reviewStatus) {
        ThrowUtils.throwIfTrue(
                reviewStatus != null && (reviewStatus < QuestionReviewConstant.REVIEW_STATUS_PENDING ||
                        reviewStatus > QuestionReviewConstant.REVIEW_STATUS_REJECTED),
                HttpStatusEnum.BAD_REQUEST,
                "审核状态不正确");
    }
    
    /**
     * 验证审核意见
     *
     * @param reviewMessage 审核意见
     */
    private void validateReviewMessage(String reviewMessage) {
        ThrowUtils.throwIfTrue(
                StringUtils.isNotBlank(reviewMessage) &&
                        reviewMessage.length() > QuestionReviewConstant.REVIEW_MESSAGE_MAX_LENGTH,
                HttpStatusEnum.BAD_REQUEST,
                "审核意见长度不能超过" + QuestionReviewConstant.REVIEW_MESSAGE_MAX_LENGTH + "个字符");
    }
}




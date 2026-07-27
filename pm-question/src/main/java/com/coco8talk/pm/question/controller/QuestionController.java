package com.coco8talk.pm.question.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.question.model.dto.CreateQuestionDTO;
import com.coco8talk.pm.question.model.dto.DeleteQuestionDTO;
import com.coco8talk.pm.question.model.dto.EditQuestionDTO;
import com.coco8talk.pm.question.model.dto.QueryQuestionDTO;
import com.coco8talk.pm.question.model.vo.QuestionForAdminVO;
import com.coco8talk.pm.question.model.vo.QuestionVO;
import com.coco8talk.pm.question.service.QuestionService;
import com.coco8talk.pm.common.util.IdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制类
 *
 * @author coco8talk
 * @since 2025/6/27 16:58
 */
@RestController
@RequestMapping("/questions")
@Tag(name = "题目相关接口", description = "提供题目的创建、编辑、删除、详情查询及分页检索能力")
@Slf4j
public class QuestionController {
    
    private final QuestionService questionService;
    
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    /**
     * 新建题目（管理员）
     *
     * @param createQuestionDTO 新建的题目信息
     * @return 新建题目的Id
     */
    @PostMapping("/admin")
    @Operation(summary = "新建题目（管理员）", description = "由管理员创建题目并返回新题目的唯一标识")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Long> adminCreateQuestion(@RequestBody @Valid CreateQuestionDTO createQuestionDTO) {
        log.info("管理员创建题目，参数：{}", createQuestionDTO);
        return questionService.adminCreateQuestion(createQuestionDTO);
    }
    
    /**
     * 删除题目（管理员）
     *
     * @param deleteQuestionDTO 题目Id封装类
     * @return 删除结果
     */
    @DeleteMapping("/admin")
    @Operation(summary = "删除题目（管理员）", description = "由管理员按题目标识删除单个题目并触发关联数据清理")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Void> adminDeleteQuestion(@RequestBody @Valid DeleteQuestionDTO deleteQuestionDTO) {
        log.info("管理员删除题目，参数：{}", deleteQuestionDTO);
        return questionService.adminDeleteQuestion(deleteQuestionDTO);
    }
    
    /**
     * 管理员批量删除题目
     *
     * @param questionIds 题目Id列表
     * @return 删除结果
     */
    @DeleteMapping("/admin/batch")
    @Operation(summary = "批量删除（管理员）", description = "由管理员批量删除指定题目并触发关联数据清理")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Void> adminBatchDeleteQuestion(@RequestBody List<Long> questionIds) {
        log.info("管理员批量删除题目，参数：{}", questionIds);
        return questionService.adminBatchDeleteQuestion(questionIds);
    }
    
    /**
     * 编辑题目信息（管理员）
     *
     * @param editQuestionDTO 编辑信息参数封装类
     * @return 编辑结果
     */
    @PutMapping("/admin")
    @Operation(summary = "编辑题目（管理员）", description = "由管理员更新指定题目的内容、标签、难度等信息")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Boolean> adminEditQuestion(@RequestBody @Valid EditQuestionDTO editQuestionDTO) {
        log.info("管理员编辑题目，参数：{}", editQuestionDTO);
        return questionService.adminEditQuestion(editQuestionDTO);
    }
    
    /**
     * 分页查询题目（管理员）
     *
     * @param queryQuestionDTO 查询条件
     * @return 查询结果
     */
    @PostMapping("/admin/search")
    @Operation(summary = "分页查询（管理员）", description = "由管理员按组合条件分页查询题目及其管理视图信息")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Page<QuestionForAdminVO>> adminQueryQuestionPage(@RequestBody @Valid QueryQuestionDTO queryQuestionDTO) {
        log.info("管理员分页查询题目，参数：{}", queryQuestionDTO);
        return questionService.adminQueryQuestionPage(queryQuestionDTO);
    }
    
    // ==================== 普通用户接口 ====================
    
    /**
     * 用户创建题目
     *
     * @param createQuestionDTO 创建题目信息
     * @return 创建结果
     */
    @PostMapping("")
    @Operation(summary = "创建题目", description = "由当前登录用户提交新题目并返回题目标识")
    public Result<Long> createQuestion(@RequestBody @Valid CreateQuestionDTO createQuestionDTO) {
        log.info("用户创建题目，参数：{}", createQuestionDTO);
        return questionService.createQuestion(createQuestionDTO);
    }
    
    /**
     * 用户删除自己的题目
     *
     * @param deleteQuestionDTO 删除信息
     * @return 删除结果
     */
    @DeleteMapping("")
    @Operation(summary = "删除题目", description = "由当前登录用户删除本人创建的指定题目")
    public Result<Void> deleteQuestion(@RequestBody @Valid DeleteQuestionDTO deleteQuestionDTO) {
        log.info("用户删除题目，参数：{}", deleteQuestionDTO);
        return questionService.deleteQuestion(deleteQuestionDTO);
    }
    
    /**
     * 用户编辑自己的题目
     *
     * @param editQuestionDTO 编辑信息
     * @return 编辑结果
     */
    @PutMapping("")
    @Operation(summary = "编辑题目", description = "由当前登录用户更新本人创建的指定题目")
    public Result<Boolean> editQuestion(@RequestBody @Valid EditQuestionDTO editQuestionDTO) {
        log.info("用户编辑题目，参数：{}", editQuestionDTO);
        return questionService.editQuestion(editQuestionDTO);
    }
    
    /**
     * 根据Id查询题目
     *
     * @param idStr 题目Id（字符串形式，避免大数精度丢失）
     * @return 脱敏的题目信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询题目", description = "按题目标识查询已审核通过且当前用户有权查看的题目详情")
    public Result<QuestionVO> queryQuestionById(@PathVariable("id") String idStr) {
        Long id = IdUtils.parseId(idStr, "题目ID");
        log.info("查询题目，题目ID：{}", id);
        return questionService.queryQuestionById(id);
    }
    
    /**
     * 分页查询题目（普通用户）
     *
     * @param queryQuestionDTO 查询条件封装类
     * @return 查询结果
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询题目", description = "按标题、标签、难度等条件分页查询可公开访问的题目")
    public Result<Page<QuestionVO>> queryQuestionPage(@RequestBody @Valid QueryQuestionDTO queryQuestionDTO) {
        log.info("分页查询题目，参数：{}", queryQuestionDTO);
        return questionService.queryQuestionPage(queryQuestionDTO);
    }
}

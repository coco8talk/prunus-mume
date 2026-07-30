package com.coco8talk.pm.question.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.question.model.dto.CreateQuestionDTO;
import com.coco8talk.pm.question.model.dto.EditQuestionDTO;
import com.coco8talk.pm.question.model.dto.QueryQuestionDTO;
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
     * 管理员批量删除题目
     *
     * @param questionIds 题目Id列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除（管理员）", description = "由管理员批量删除指定题目并触发关联数据清理")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Void> adminBatchDeleteQuestion(@RequestBody List<Long> questionIds) {
        log.info("管理员批量删除题目，参数：{}", questionIds);
        return questionService.adminBatchDeleteQuestion(questionIds);
    }
    
    /**
     * 用户创建题目
     *
     * @param createQuestionDTO 创建题目信息
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建题目", description = "管理员创建的题目自动通过审核，其他用户提交后进入待审核状态")
    public Result<Long> createQuestion(@RequestBody @Valid CreateQuestionDTO createQuestionDTO) {
        log.info("创建题目，参数：{}", createQuestionDTO);
        return questionService.createQuestionForCaller(createQuestionDTO);
    }
    
    /**
     * 用户删除自己的题目
     *
     * @param idStr 题目Id（字符串形式，避免大数精度丢失）
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目", description = "资源所有者或管理员可删除指定题目")
    public Result<Void> deleteQuestion(@PathVariable("id") String idStr) {
        Long id = IdUtils.parseId(idStr, "题目ID");
        log.info("删除题目，题目ID：{}", id);
        return questionService.deleteQuestionForCaller(id);
    }
    
    /**
     * 用户编辑自己的题目
     *
     * @param editQuestionDTO 编辑信息
     * @return 编辑结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑题目", description = "资源所有者或管理员可更新指定题目")
    public Result<Boolean> editQuestion(@PathVariable("id") String idStr,
                                        @RequestBody @Valid EditQuestionDTO editQuestionDTO) {
        Long id = IdUtils.parseId(idStr, "题目ID");
        editQuestionDTO.setId(id);
        log.info("编辑题目，参数：{}", editQuestionDTO);
        return questionService.editQuestionForCaller(editQuestionDTO);
    }
    
    /**
     * 根据Id查询题目
     *
     * @param idStr 题目Id（字符串形式，避免大数精度丢失）
     * @return 管理员视图或公开视图
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询题目", description = "管理员可查看任意审核状态和完整字段，其他调用者仅可查看已审核通过的公开字段")
    public Result<Object> queryQuestionById(@PathVariable("id") String idStr) {
        Long id = IdUtils.parseId(idStr, "题目ID");
        log.info("查询题目，题目ID：{}", id);
        return questionService.queryQuestionByIdForCaller(id);
    }
    
    /**
     * 分页查询题目（普通用户）
     *
     * @param queryQuestionDTO 查询条件封装类
     * @return 查询结果
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询题目", description = "管理员返回完整字段和任意审核状态，其他调用者返回已审核通过的公开字段")
    public Result<Object> queryQuestionPage(@RequestBody @Valid QueryQuestionDTO queryQuestionDTO) {
        log.info("分页查询题目，参数：{}", queryQuestionDTO);
        return questionService.queryQuestionPageForCaller(queryQuestionDTO);
    }
}

package com.coco8talk.pm.question.bank.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.question.bank.model.dto.AddQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.DeleteQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.EditQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.dto.QueryQuestionBankDTO;
import com.coco8talk.pm.question.bank.model.vo.QuestionBankDetailVO;
import com.coco8talk.pm.question.bank.service.QuestionBankService;
import com.coco8talk.pm.common.util.IdUtils;
import com.coco8talk.pm.common.util.ObjectMyUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 题库控制类
 *
 * @author coco8talk
 * @since 2025/6/26 21:47
 */
@RestController
@RequestMapping("/question-banks")
@Tag(name = "题库相关接口", description = "提供题库的管理、详情查询以及面向用户和管理员的分页检索能力")
@Slf4j
public class QuestionBankController {
    
    private final QuestionBankService questionBankService;
    
    public QuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }
    
    /**
     * 管理员新建题库
     *
     * @param addQuestionBankDTO 新建的题库信息
     * @return 新建题库的Id
     */
    @PostMapping
    @Operation(summary = "新建题库（管理员）", description = "由管理员创建题库并返回新题库的唯一标识")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Long> adminCreateQuestionBank(@RequestBody @Valid AddQuestionBankDTO addQuestionBankDTO) {
        log.info("管理员新建题库，参数：{}", addQuestionBankDTO);
        return questionBankService.adminCreateQuestionBank(addQuestionBankDTO);
    }
    
    /**
     * 管理员删除题库
     *
     * @param questionBankIdStr 题库Id（字符串形式，避免大数精度丢失）
     * @return 删除结果，成功-true
     */
    @DeleteMapping("/{questionBankId}")
    @Operation(summary = "删除题库（管理员）", description = "由管理员删除指定题库及其题目关联关系")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Boolean> adminDeleteQuestionBank(
        @PathVariable("questionBankId") String questionBankIdStr) {
        Long questionBankId = IdUtils.parseId(questionBankIdStr, "题库ID");
        DeleteQuestionBankDTO deleteQuestionDTO = new DeleteQuestionBankDTO();
        deleteQuestionDTO.setId(questionBankId);
        log.info("管理员删除题库，参数：{}", deleteQuestionDTO);
        return questionBankService.adminDeleteQuestionBank(deleteQuestionDTO);
    }
    
    /**
     * 管理员编辑题库（管理员）
     *
     * @param editQuestionBankDTO 需要编辑的信息
     * @return 编辑结果 成功-true
     */
    @PutMapping("/{questionBankId}")
    @Operation(summary = "编辑题库（管理员）", description = "由管理员更新指定题库的名称、描述、封面等信息")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Boolean> editQuestionBankOnlyAdmin(
        @PathVariable("questionBankId") String questionBankIdStr,
        @RequestBody @Valid EditQuestionBankDTO editQuestionBankDTO) {
        Long questionBankId = IdUtils.parseId(questionBankIdStr, "题库ID");
        editQuestionBankDTO.setId(questionBankId);
        log.info("管理员编辑题库，参数：{}", editQuestionBankDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(editQuestionBankDTO, HttpStatusEnum.BAD_REQUEST, "请输入需要编辑的信息");
        return questionBankService.adminEditQuestionBank(editQuestionBankDTO);
    }
    
    /**
     * 根据Id查询题库
     *
     * @param questionBankIdStr 题库Id（字符串形式，避免大数精度丢失）
     * @param needQueryQuestionList 是否需要题目列表
     * @return 查询结果，题库脱敏信息
     */
    @GetMapping("/{questionBankId}")
    @Operation(summary = "查询题库", description = "按题库标识查询题库详情，并可选择同时加载题目列表")
    public Result<QuestionBankDetailVO> queryQuestionBankById(@PathVariable("questionBankId") String questionBankIdStr,
                                                              @RequestParam(defaultValue = "false") Boolean needQueryQuestionList) {
        Long questionBankId = IdUtils.parseId(questionBankIdStr, "题库ID");
        log.info("查询题库，参数：questionBankId={}, needQueryQuestionList={}", questionBankId, needQueryQuestionList);
        return questionBankService.queryQuestionBankByIdForCaller(questionBankId, needQueryQuestionList);
    }
    
    /**
     * 分页查询题库
     *
     * @param queryQuestionBankDTO 查询条件
     * @return 分页查询结果
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询题库", description = "管理员返回完整字段，其他调用者返回公开字段")
    public Result<Object> queryPageQuestionBank(@RequestBody @Valid QueryQuestionBankDTO queryQuestionBankDTO) {
        log.info("分页查询题库，参数：{}", queryQuestionBankDTO);
        return questionBankService.queryPageQuestionBankForCaller(queryQuestionBankDTO);
    }

}

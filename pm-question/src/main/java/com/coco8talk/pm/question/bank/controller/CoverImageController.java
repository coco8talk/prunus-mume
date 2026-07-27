package com.coco8talk.pm.question.bank.controller;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.question.bank.service.CoverImageService;
import com.coco8talk.pm.common.util.IdUtils;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 题库封面图片生成接口。
 * 支持为单个题库或所有题库生成文字封面并上传至腾讯云 COS。
 *
 * @author coco8talk
 */
@RestController
@RequestMapping("/covers")
@Tag(name = "题库封面图片生成接口", description = "提供题库文字封面的生成、上传和题库封面地址回写能力")
@Slf4j
public class CoverImageController {

    private final CoverImageService coverImageService;

    public CoverImageController(CoverImageService coverImageService) {
        this.coverImageService = coverImageService;
    }

    /**
     * 为指定题库生成封面图片。
     */
    @PostMapping("/question-bank/{id}")
    @Operation(summary = "为指定题库生成封面图片", description = "由管理员为指定题库生成 PNG 文字封面、上传至腾讯云 COS 并回写地址")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<String> generateBankCover(@PathVariable("id") String idStr) {
        Long id = IdUtils.parseId(idStr, "题库ID");
        log.info("请求为题库 [{}] 生成封面图片", id);
        return coverImageService.generateAndSave(id);
    }

    /**
     * 批量为所有题库生成封面图片。
     */
    @PostMapping("/question-bank/batch")
    @Operation(summary = "批量为所有题库生成封面图片", description = "由管理员为全部题库逐一生成并保存腾讯云 COS 封面图片")
    @SaCheckRole(AuthConstant.ADMIN_USER_ROLE)
    public Result<Integer> generateAllBankCovers() {
        log.info("请求批量为所有题库生成封面图片");
        return coverImageService.generateForAllBanks();
    }
}

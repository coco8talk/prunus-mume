package com.coco8talk.pm.question.bank.service.impl;
import com.coco8talk.pm.question.bank.service.QuestionBankService;
import com.coco8talk.pm.question.bank.service.CoverImageService;

import cn.hutool.core.util.StrUtil;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.coco8talk.pm.question.bank.model.entity.QuestionBank;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * 题库封面图片生成与存储服务实现。
 * 使用 Java Graphics2D 渲染中文标题文字到 PNG 图片，
 * 通过腾讯云 COS SDK 上传至对象存储，并回写 URL 到题库记录。
 *
 * @author coco8talk
 */
@Service
@Slf4j
public class CoverImageServiceImpl implements CoverImageService {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 450;
    private static final String FONT_NAME = "PingFang SC";
    private static final int TITLE_FONT_SIZE = 48;
    private static final int SUBTITLE_FONT_SIZE = 18;
    private static final Color BG_START = new Color(37, 99, 235);   // primary blue
    private static final Color BG_END = new Color(99, 102, 241);    // indigo
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final String SUBTITLE = "面试练习平台 · 题库";
    private static final String COS_KEY_PREFIX = "bank_";

    private final QuestionBankService questionBankService;
    private final COSClient cosClient;

    @Value("${coco8talk.tencent.cos.bucketName}")
    private String bucketName;
    @Value("${coco8talk.tencent.cos.region}")
    private String region;

    public CoverImageServiceImpl(QuestionBankService questionBankService, COSClient cosClient) {
        this.questionBankService = questionBankService;
        this.cosClient = cosClient;
    }

    @Override
    public Result<String> generateAndSave(Long bankId) {
        // 1. 获取题库信息
        QuestionBank bank = questionBankService.getById(bankId);
        ThrowUtils.throwIfNull(bank, HttpStatusEnum.NOT_FOUND, "题库不存在");
        ThrowUtils.throwIfBlank(bank.getTitle(), HttpStatusEnum.BAD_REQUEST, "题库名称为空");

        String title = bank.getTitle();
        log.info("开始为题库 [{}] 生成封面图片", title);

        // 2. 生成图片字节
        byte[] imageBytes = generateCoverImage(title);

        // 3. 上传至 COS
        String cosKey = COS_KEY_PREFIX + bankId + ".png";
        uploadToCos(cosKey, imageBytes);

        // 4. 构建 COS URL 并回写数据库
        String imageUrl = StrUtil.format(
            "https://{}.cos.{}.myqcloud.com/{}",
            bucketName, region, cosKey
        );

        QuestionBank update = new QuestionBank();
        update.setId(bankId);
        update.setPicture(imageUrl);
        questionBankService.updateById(update);

        log.info("题库 [{}] 封面图片生成完成: {}", title, imageUrl);
        return Result.success(HttpStatusEnum.OK, imageUrl);
    }

    @Override
    public Result<Integer> generateForAllBanks() {
        // 查询所有题库（含已删除）
        List<QuestionBank> banks = questionBankService.lambdaQuery().list();
        int count = 0;
        for (QuestionBank bank : banks) {
            try {
                generateAndSave(bank.getId());
                count++;
            } catch (Exception e) {
                log.error("为题库 [{}] 生成封面图片失败", bank.getTitle(), e);
            }
        }
        log.info("批量生成封面图片完成，成功处理 {} 个题库", count);
        return Result.success(HttpStatusEnum.OK, count);
    }

    // ---------- 私有方法 ----------

    /**
     * 使用 Java Graphics2D 生成带文字渐变背景的 PNG 封面图片。
     */
    private byte[] generateCoverImage(String title) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 绘制渐变背景
            GradientPaint gradient = new GradientPaint(
                0, 0, BG_START,
                WIDTH, HEIGHT, BG_END
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // 加载中文字体
            Font titleFont = loadFont(TITLE_FONT_SIZE, true);
            Font subtitleFont = loadFont(SUBTITLE_FONT_SIZE, false);

            // 绘制标题（居中）
            g2d.setFont(titleFont);
            g2d.setColor(TEXT_COLOR);
            FontRenderContext frc = g2d.getFontRenderContext();
            Rectangle2D titleBounds = titleFont.getStringBounds(title, frc);
            int titleX = (int) ((WIDTH - titleBounds.getWidth()) / 2);
            int titleY = (int) ((HEIGHT + titleBounds.getHeight()) / 2) - 20;
            g2d.drawString(title, titleX, titleY);

            // 绘制副标题（标题下方）
            g2d.setFont(subtitleFont);
            g2d.setColor(new Color(255, 255, 255, 180));
            Rectangle2D subBounds = subtitleFont.getStringBounds(SUBTITLE, frc);
            int subX = (int) ((WIDTH - subBounds.getWidth()) / 2);
            int subY = titleY + (int) subBounds.getHeight() + 16;
            g2d.drawString(SUBTITLE, subX, subY);

        } finally {
            g2d.dispose();
        }

        // 输出为 PNG 字节数组
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成封面图片失败", e);
        }
    }

    /**
     * 加载中文字体，优先使用 PingFang SC，回退到系统默认字体。
     */
    private Font loadFont(int size, boolean bold) {
        Font font = new Font(FONT_NAME, bold ? Font.BOLD : Font.PLAIN, size);
        // 检查字体是否可用，不可用时回退到 Dialog 逻辑字体（支持中文）
        if (font.getFamily().contains("PingFang") || font.canDisplay('中')) {
            return font;
        }
        return new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size);
    }

    /**
     * 将图片字节流上传至腾讯云 COS。
     */
    private void uploadToCos(String cosKey, byte[] imageBytes) {
        InputStream inputStream = new ByteArrayInputStream(imageBytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBytes.length);
        metadata.setContentType("image/png");

        PutObjectRequest request = new PutObjectRequest(bucketName, cosKey, inputStream, metadata);
        try {
            cosClient.putObject(request);
        } catch (Exception e) {
            throw new RuntimeException("上传封面图片到 COS 失败: " + e.getMessage(), e);
        }
    }
}

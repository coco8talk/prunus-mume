package com.coco8talk.pm.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端工具类
 * 用于获取客IP地址
 *
 * @author coco8talk
 * @since 2025/7/18 0:06
 **/
public class ClientUtil {

    /**
     * 根据请求获取设备信息
     **/
    public static String getRequestDevice() {
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        ThrowUtils.throwIfNull(attributes, HttpStatusEnum.INTERNAL_SERVER_ERROR, "当前线程不是 HTTP 请求线程！");
        HttpServletRequest request = attributes.getRequest();
        String userAgentStr = request.getHeader(Header.USER_AGENT.toString());
        // 使用 Hutool 解析 UserAgent
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        ThrowUtils.throwIfNull(userAgent, HttpStatusEnum.BAD_REQUEST, "非法请求");
        // 默认值是 PC
        String device = "pc";
        // 是否为小程序
        if (isMiniProgram(userAgentStr)) {
            device = "miniProgram";
        } else if (isPad(userAgentStr)) {
            // 是否为 Pad
            device = "pad";
        } else if (userAgent.isMobile()) {
            // 是否为手机
            device = "mobile";
        }
        return device;
    }
    
    /**
     * 判断是否是小程序
     * 一般通过 User-Agent 字符串中的 "MicroMessenger" 来判断是否是微信小程序
     **/
    private static boolean isMiniProgram(String userAgentStr) {
        // 判断 User-Agent 是否包含 "MicroMessenger" 表示是微信环境
        return StrUtil.containsIgnoreCase(userAgentStr, "MicroMessenger")
            && StrUtil.containsIgnoreCase(userAgentStr, "MiniProgram");
    }
    
    /**
     * 判断是否为平板设备
     * 支持 iOS（如 iPad）和 Android 平板的检测
     **/
    private static boolean isPad(String userAgentStr) {
        // 检查 iPad 的 User-Agent 标志
        boolean isIpad = StrUtil.containsIgnoreCase(userAgentStr, "iPad");
        
        // 检查 Android 平板（包含 "Android" 且不包含 "Mobile"）
        boolean isAndroidTablet = StrUtil.containsIgnoreCase(userAgentStr, "Android")
            && !StrUtil.containsIgnoreCase(userAgentStr, "Mobile");
        
        // 如果是 iPad 或 Android 平板，则返回 true
        return isIpad || isAndroidTablet;
    }
}

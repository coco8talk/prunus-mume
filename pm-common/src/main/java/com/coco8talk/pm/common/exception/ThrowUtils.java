package com.coco8talk.pm.common.exception;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * 异常快捷抛出工具类
 *
 * @author coco8talk
 * @since 2025/6/23 14:35
 **/
public class ThrowUtils {
    
    /**
     * 如果 expression 为 true，则抛出异常
     *
     * @param expression     布尔值
     * @param httpStatusEnum 响应状态
     * @param trace          响应信息
     */
    public static void throwIfTrue(boolean expression, HttpStatusEnum httpStatusEnum, String trace) {
        if (expression) {
            throw new BizException(httpStatusEnum.getCode(), trace);
        }
    }
    
    /**
     * 如果 expression 为 false，则抛出异常
     *
     * @param expression     布尔值
     * @param httpStatusEnum 响应状态
     * @param trace          响应信息
     */
    public static void throwIfFalse(boolean expression, HttpStatusEnum httpStatusEnum, String trace) {
        if (!expression) {
            throw new BizException(httpStatusEnum.getCode(), trace);
        }
    }
    
    /**
     * 如果 object 为Null，则抛出异常
     *
     * @param object         要检查的对象
     * @param httpStatusEnum 错误类型
     */
    public static void throwIfNull(Object object, HttpStatusEnum httpStatusEnum, String trace) {
        if (object == null) {
            throw new BizException(httpStatusEnum.getCode(), trace);
        }
    }
    
    /**
     * 如果 object 不为Null，则抛出异常
     *
     * @param object         要检查的对象
     * @param httpStatusEnum 错误类型
     */
    public static void throwIfNotNull(Object object, HttpStatusEnum httpStatusEnum, String trace) {
        if (object != null) {
            throw new BizException(httpStatusEnum.getCode(), trace);
        }
    }
    
    /**
     * 如果 字符串 为 null 或者为 空字符串 或者为 空格字符串
     *
     * @param str            待检查的字符串
     * @param httpStatusEnum 错误类型
     * @param trace          错误信息
     */
    public static void throwIfBlank(String str, HttpStatusEnum httpStatusEnum, String trace) {
        if (StringUtils.isBlank(str)) {
            throw new BizException(httpStatusEnum.getCode(), trace);
        }
    }
}

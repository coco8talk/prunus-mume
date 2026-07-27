package com.coco8talk.pm.common.util;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.BizException;
import com.coco8talk.pm.common.exception.ThrowUtils;

/**
 * ID转换工具类
 * 用于安全地将字符串ID转换为Long类型
 * 
 * @author coco8talk
 * @since 2025/1/20
 */
public class IdUtils {
    
    /**
     * 将字符串ID安全转换为Long类型
     * 
     * @param idStr 字符串形式的ID
     * @param fieldName 字段名称，用于错误提示
     * @return Long类型的ID
     * @throws BizException 当ID格式不正确时
     */
    public static Long parseId(String idStr, String fieldName) {
        ThrowUtils.throwIfTrue(idStr == null || idStr.trim().isEmpty(), 
            HttpStatusEnum.BAD_REQUEST, fieldName + "不能为空");
        
        try {
            long id = Long.parseLong(idStr.trim());
            ThrowUtils.throwIfTrue(id <= 0, HttpStatusEnum.BAD_REQUEST, fieldName + "必须大于0");
            return id;
        } catch (NumberFormatException e) {
            throw new BizException(HttpStatusEnum.BAD_REQUEST.getCode(), fieldName + "格式不正确");
        }
    }
} 
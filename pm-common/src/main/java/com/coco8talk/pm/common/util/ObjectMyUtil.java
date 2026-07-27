package com.coco8talk.pm.common.util;


import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.BizException;
import com.coco8talk.pm.common.exception.ThrowUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对象 工具类
 *
 * @author coco8talk
 * @since 2025/6/24 13:28
 **/
public class ObjectMyUtil {
    
    /**
     * 检查对象中是否所有字段都为空或空白字符串，如果是，则抛出异常
     * 此方法用于确保对象中至少有一个字段是非空的，以避免潜在的数据完整性问题
     * 会递归检查所有父类的字段
     *
     * @param obj            要检查的对象
     * @param httpStatusEnum HTTP状态码枚举，用于抛出异常时指定状态码
     * @param trace          异常追踪信息，用于调试
     * @throws BizException 如果无法访问对象的字段
     */
    public static void throwIfAllFieldsAreEmptyOrBlank(Object obj, HttpStatusEnum httpStatusEnum, String trace) {
        
        // 首先确保对象本身不为空
        ThrowUtils.throwIfNull(obj, httpStatusEnum, trace);
        
        // 获取对象的所有字段（包括父类字段）
        List<Field> allFields = getAllFields(obj.getClass());
        
        // 默认假设没有非空字段
        boolean hasAnyNonEmptyField = false;
        
        // 遍历所有字段，检查是否有非空字段
        for (Field field : allFields) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new BizException(HttpStatusEnum.BAD_REQUEST.getCode(), "无法访问对象的字段");
            }
            
            // 如果字段值不为空，进一步检查其类型
            if (value != null) {
                if (value instanceof String str) {
                    // 对于字符串类型，检查是否为空白字符串
                    if (!str.trim().isEmpty()) {
                        hasAnyNonEmptyField = true;
                        break; // 找到一个非空字段即可，结束循环
                    }
                } else {
                    // 非字符串类型只要不为 null 就算有效字段
                    hasAnyNonEmptyField = true;
                    break;
                }
            }
        }
        
        // 如果没有任何非空字段，抛出异常
        ThrowUtils.throwIfFalse(hasAnyNonEmptyField, httpStatusEnum, trace);
    }
    
    /**
     * 获取类的所有字段，包括父类的字段
     *
     * @param clazz 要检查的类
     * @return 包含所有字段的列表
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        
        // 递归获取所有父类的字段
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            clazz = clazz.getSuperclass();
        }
        
        return fields;
    }
    
}

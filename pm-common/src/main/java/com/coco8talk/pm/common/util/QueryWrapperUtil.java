package com.coco8talk.pm.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coco8talk.pm.common.CommonConstant;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 查询条件构建工具类
 * 用于处理MyBatis-Plus查询条件的通用逻辑，避免重复代码
 *
 * @author coco8talk
 * @since 2025/6/30 18:00
 **/
public class QueryWrapperUtil {
    
    /**
     * 为查询条件添加排序逻辑
     *
     * @param wrapper   查询条件包装器
     * @param sortField 排序字段
     * @param sortOrder 排序方式
     * @param <T>       实体类型
     * @return 添加了排序条件的查询包装器
     */
    public static <T> LambdaQueryWrapper<T> applySorting(LambdaQueryWrapper<T> wrapper, String sortField, String sortOrder) {
        if (StringUtils.isNoneBlank(sortField, sortOrder)) {
            boolean isAsc = CommonConstant.ASC.equals(sortOrder);
            String underlineField = camelToUnderLine(sortField);
            if (validateSortFieldSecurity(sortField)) {
                wrapper.last("ORDER BY " + underlineField + (isAsc ? " ASC" : " DESC"));
            }
        }
        return wrapper;
    }

    /**
     * 验证排序字段是否包含可能引发 SQL 注入风险的字符
     *
     * @param sortField 待验证的排序字段字符串
     * @return 如果排序字段包含潜在风险字符，则返回 true；否则返回 false
     */
    public static boolean validateSortFieldSecurity(String sortField) {

        // 如果排序字段为 null，直接返回 false
        if (sortField == null) {
            return true;
        }

        // 使用 StringUtils.containsAny 方法判断排序字段是否包含以下特殊字符： "=", "(", ")", 空格(" ")
        // 若包含任意一个，则认为存在 SQL 注入风险，返回 true
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }

    /**
     * 将驼峰命名转换为下划线命名
     *
     * @param camel 驼峰命名的字符串
     * @return 转换后的下划线命名字符串
     * @throws IllegalArgumentException 如果输入字符串为空或不合法，则抛出异常
     */
    public static String camelToUnderLine(String camel) {
        // 检查输入字符串是否为空或不合法
        ThrowUtils.throwIfBlank(camel, HttpStatusEnum.INTERNAL_SERVER_ERROR, "待转换字符串不合法");

        // 使用StringBuilder来构建转换后的字符串
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            // 当前字符为大写时，前面添加下划线并将该字符转为小写
            if (Character.isUpperCase(c)) {
                builder.append("_").append(Character.toLowerCase(c));
            } else {
                // 如果当前字符不是大写，则直接添加到StringBuilder中
                builder.append(c);
            }
        }
        // 返回构建好的下划线命名字符串
        return builder.toString();
    }
}
package com.coco8talk.pm.common.util;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;

import java.util.function.Function;

/**
 * 实体校验工具类
 * 统一处理ID校验和存在性检查，避免重复代码
 *
 * @author coco8talk
 * @since 2025/1/16
 */
public class DtoValidationUtil {
    
    /**
     * 校验ID格式并检查实体是否存在（使用IService）
     *
     * @param id         待校验的ID
     * @param service    对应的Service
     * @param entityName 实体名称（用于错误提示）
     * @param <T>        实体类型
     * @return 查询到的实体对象
     */
    public static <T> T validateEntityExists(Long id, IService<T> service, String entityName) {
        // 校验ID格式
        ThrowUtils.throwIfFalse(
                id != null && id > 0,
                HttpStatusEnum.BAD_REQUEST,
                "请传入正确的" + entityName + "Id");
        
        // 检查实体是否存在
        T entity = service.getById(id);
        ThrowUtils.throwIfNull(entity, HttpStatusEnum.BAD_REQUEST, entityName + "不存在");
        
        return entity;
    }
    
    /**
     * 校验ID格式并检查实体是否存在（使用自定义查询函数）
     *
     * @param id            待校验的ID
     * @param queryFunction 查询函数
     * @param entityName    实体名称（用于错误提示）
     * @param <T>           实体类型
     * @return 查询到的实体对象
     */
    public static <T> T validateEntityExists(Long id, Function<Long, T> queryFunction, String entityName) {
        // 校验ID格式
        ThrowUtils.throwIfFalse(
                id != null && id > 0,
                HttpStatusEnum.BAD_REQUEST,
                "请传入正确的" + entityName + "Id");
        
        // 检查实体是否存在
        T entity = queryFunction.apply(id);
        ThrowUtils.throwIfNull(entity, HttpStatusEnum.BAD_REQUEST, entityName + "不存在");
        
        return entity;
    }
    
    /**
     * 仅校验ID格式，不检查存在性（允许为null）
     *
     * @param id         待校验的ID
     * @param entityName 实体名称（用于错误提示）
     */
    public static void validateIdFormat(Long id, String entityName) {
        ThrowUtils.throwIfTrue(
                id != null && id <= 0,
                HttpStatusEnum.BAD_REQUEST,
                "请传入合法的" + entityName + "Id");
    }
    
    /**
     * 仅校验ID格式，不检查存在性（不允许为null）
     *
     * @param id         待校验的ID
     * @param entityName 实体名称（用于错误提示）
     */
    public static void validateIdNotNullFormat(Long id, String entityName) {
        ThrowUtils.throwIfFalse(
                id != null && id > 0,
                HttpStatusEnum.BAD_REQUEST,
                "请传入合法的" + entityName + "Id");
    }
    
    /**
     * 检验分页查询页码参数是否合法
     *
     * @param current 待检查的页码
     */
    public static void validatePageCurrentFormat(Integer current) {
        ThrowUtils.throwIfFalse(current != null && current > 0, HttpStatusEnum.BAD_REQUEST, "请输入正确的查询页码");
    }
    
    /**
     * 检查分页查询单页最多限制数是否合法
     *
     * @param pageSize 单页最多限制数
     */
    public static void validatePageSizeFormat(Integer pageSize) {
        ThrowUtils.throwIfTrue(pageSize != null && (pageSize < 0 || pageSize > 20),
                HttpStatusEnum.BAD_REQUEST,
                "请输入正确的当页查询最大限制条数，且最多不可超过20条");
    }
}
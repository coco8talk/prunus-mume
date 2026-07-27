package com.coco8talk.pm.common.dto;

import com.coco8talk.pm.common.constant.CommonConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 基础查询DTO，包含公共的分页和排序参数
 *
 * @author coco8talk
 * @since 2025/6/30 10:00
 **/
@Data
public class BaseQueryDTO {
    
    /**
     * 请求的页码，用于计算数据的起始位置
     */
    @Positive(message = "请传入合法的页码")
    private Integer current;
    
    /**
     * 每页包含的数量，用于限定每页数量的最大上限
     */
    @Positive(message = "请传入合法的每页上限")
    @Max(value = CommonConstant.PAGE_SIZE_MAX, message = "最多一页可查询" + CommonConstant.PAGE_SIZE_MAX + "条")
    private Integer pageSize;
    
    /**
     * 排序依据字段
     */
    @Size(max = CommonConstant.SORT_FIELD_MAX_LENGTH, message = "排序字段长度不能超过" + CommonConstant.SORT_FIELD_MAX_LENGTH)
    private String sortField;
    
    /**
     * 排序方式（descend-降序 ascend-升序 undefined-不排序）
     */
    @Size(max = CommonConstant.SORT_ORDER_MAX_LENGTH, message = "排序方式字段长度不能超过" + CommonConstant.SORT_ORDER_MAX_LENGTH)
    private String sortOrder;
} 
package com.coco8talk.pm.common;

import com.coco8talk.pm.common.HttpStatusEnum;
import lombok.Getter;

/**
 * @author coco8talk
 * @since 2025/6/23 15:48
 **/
@Getter
public class Result<T> {
    
    /**
     * 响应状态码
     */
    private final int code;
    /**
     * 响应数据
     */
    private final T data;
    /**
     * 响应消息
     */
    private final String message;
    
    /**
     * 私有构造参数
     * @param code 响应状态码
     * @param data 响应数据
     * @param message 响应详细
     */
    private Result(int code, T data, String message){
        this.code = code;
        this.data = data;
        this.message = message;
    }
    
    //--------------------------------------------success--------------------------------------------
    /**
     * 创建成功响应，无数据
     * @param httpStatusEnum 响应状态码
     * @return 响应成功结果
     * @param <T> 数据泛型
     */
    public static <T> Result<T> success(HttpStatusEnum httpStatusEnum){
        return new Result<>(httpStatusEnum.getCode(), null, "操作成功");
    }
    
    /**
     * 创建成功响应，带数据
     * @param httpStatusEnum 响应状态吗
     * @param data 相应数据
     * @return 返回响应结果
     * @param <T> 数据泛型
     */
    public static <T> Result<T> success(HttpStatusEnum httpStatusEnum, T data){
        return new Result<>(httpStatusEnum.getCode(), data, httpStatusEnum.getMessage());
    }
    
    
    //----------------------------------------------fail----------------------------------------------
    /**
     * 创建错误响应
     * @param httpStatusEnum 响应状态码
     * @return 响应结果
     * @param <T> 数据泛型
     */
    public static <T> Result<T> fail(HttpStatusEnum httpStatusEnum){
        return new Result<>(httpStatusEnum.getCode(), null, httpStatusEnum.getMessage());
    }
    
    /**
     * 创建错误响应，带自定义错误信息
     * @param httpStatusEnum 响应状态码
     * @param trace 响应错误信息
     * @return 响应结果
     * @param <T> 响应泛型
     */
    public static <T> Result<T> fail(HttpStatusEnum httpStatusEnum, String trace){
        return new Result<>(httpStatusEnum.getCode(), null, trace);
    }
    
    /**
     * 创建错误响应，用于 BizException 返回错误信息
     * @param code 响应状态码
     * @param trace 响应信息
     * @return 响应结果
     * @param <T> 泛型数据
     */
    public static <T> Result<T> error(int code, String trace){
        return new Result<>(code, null, trace);
    }
}

package com.coco8talk.pm.platform.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.TimeoutException;

/**
 * 全局异常处理器
 *
 * @author coco8talk
 * @since 2025/6/23 16:12
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 处理客户端错误请求异常
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(BadRequestException.class)
    public Result<Void> handleBadRequestException(BadRequestException e) {
        log.warn("客户端请求错误: {}", e.getMessage(), e);
        return Result.fail(HttpStatusEnum.BAD_REQUEST, "请求参数错误");
    }
    
    /**
     * 处理权限不足异常
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("用户权限不足: 用户尝试访问需要角色 [{}] 的资源", e.getRole());
        return Result.fail(HttpStatusEnum.FORBIDDEN, "权限不足，无法访问该资源");
    }
    
    /**
     * 处理用户未登录异常
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录: 尝试访问需要登录的资源, 异常类型: {}", e.getType());
        return Result.fail(HttpStatusEnum.UNAUTHORIZED, "用户未登录，请先登录");
    }
    
    /**
     * 处理资源不存在异常
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("请求的资源不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
        return Result.fail(HttpStatusEnum.NOT_FOUND, "请求的资源不存在");
    }
    
    /**
     * 处理请求方式错误异常
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: 当前方法 [{}], 支持的方法: {}", e.getMethod(), e.getSupportedHttpMethods());
        return Result.fail(HttpStatusEnum.METHOD_NOT_ALLOWED, "请求方法不支持");
    }
    
    /**
     * 处理请求超时异常
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(TimeoutException.class)
    public Result<Void> handleTimeoutException(TimeoutException e) {
        log.error("请求处理超时: {}", e.getMessage(), e);
        return Result.fail(HttpStatusEnum.REQUEST_TIMEOUT, "请求处理超时，请稍后重试");
    }
    
    /**
     * 处理业务异常
     *
     * @param e 业务异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: 错误码 [{}], 错误信息: {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理其它所有未捕获异常
     * 并打印详细信息
     *
     * @param e 详细异常信息
     * @return 包含简要异常信息的返回结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部错误: {}", e.getMessage(), e);
        return Result.fail(HttpStatusEnum.INTERNAL_SERVER_ERROR, "系统内部错误，请联系管理员");
    }
}

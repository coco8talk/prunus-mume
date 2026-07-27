package com.coco8talk.pm.common.result.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码请求枚举类，用于统一处理Http响应状态
 * @author coco8talk
 * @since 2025/6/23 14:57
 **/
@Getter
@AllArgsConstructor
public enum HttpStatusEnum {
    
    //---------------------------------2xx---------------------------------
    /**
     * 200 OK-请求成功，并返回客户端希望的数据
     */
    OK(200, "操作成功"),
    /**
     * 201 CREATED-资源创建成功，表示服务器已经创建了一个新资源
     */
    CREATED(201, "资源创建成功"),
    /**
     * 204 NO_CONTENT-请求成功，但无返回内容
     */
    NO_CONTENT(204, "无内容"),
    
    //---------------------------------4xx---------------------------------
    /**
     * 400 BAD_REQUEST-客户端发送的请求有误
     */
    BAD_REQUEST(400, "请求参数错误"),
    /**
     * 401 UNAUTHORIZED-客户端未提供身份认证凭证或凭证无效
     */
    UNAUTHORIZED(401, "未授权"),
    /**
     * 403 FORBIDDEN-客户权限不足
     */
    FORBIDDEN(403, "禁止访问"),
    /**
     * 404 NOT_FOUND-服务器找不到请求的资源
     */
    NOT_FOUND(404, "资源不存在"),
    /**
     * 405 METHOD_NOT_ALLOWED-请求方法不被允许
     */
    METHOD_NOT_ALLOWED(405, "不可接受的请求"),
    /**
     * 408 REQUEST_TIMEOUT-服务器等待客户端发送请求的时间过长
     */
    REQUEST_TIMEOUT(408, "请求超时"),
    /**
     * 429 TOO_MANY_REQUESTS-请求数量过多
     */
    TOO_MANY_REQUESTS(429, "请求数量过多"),
    
    //---------------------------------5xx---------------------------------
    /**
     * 服务器遇到未知错误，无法完成请求
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");
    
    
    private final int code;
    private final String message;
}

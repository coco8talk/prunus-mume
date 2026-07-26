package com.coco8talk.pm.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;

/**
 * 自定义异常类
 *
 * @author coco8talk
 * @since 2025/6/23 14:33
 **/
@Getter
@AllArgsConstructor
public class BizException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 5617318681020291057L;
    
    private final int code;
    private final String message;
}

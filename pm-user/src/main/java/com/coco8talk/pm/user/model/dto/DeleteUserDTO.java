package com.coco8talk.pm.user.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员删除用户参数封装类
 *
 * @author coco8talk
 * @since 2025/6/25 17:14
 **/
@Data
public class DeleteUserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1769110400313757222L;
    
    /**
     * 用户ID
     */
    @Positive(message = "请传入合法的用户")
    private Long id;
}

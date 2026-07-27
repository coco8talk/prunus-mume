package com.coco8talk.pm.filestorage.service.support;

import cn.dev33.satoken.stp.StpUtil;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.ThrowUtils;
import org.springframework.stereotype.Service;

/**
 * @author coco8talk
 * @since 2025/10/16 22:33
 **/
@Service
public class GetKeyService {
    /**
     * 生成 cos key
     *
     * @return cos key
     */
    public String generateAvatarCosKey() {
        
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIfNull(userId, HttpStatusEnum.FORBIDDEN, "用户未登录");
        
        //直接使用userid方便后面confirm后写入数据库
        return userId;
    }
}

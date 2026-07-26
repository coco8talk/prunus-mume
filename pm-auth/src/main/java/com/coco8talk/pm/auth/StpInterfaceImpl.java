package com.coco8talk.pm.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.coco8talk.pm.api.auth.constant.AuthConstant;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.auth.enums.UserRoleEnums;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 * @author coco8talk
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合 
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从 Session 中获取当前登录用户的信息
        SessionUserDTO currentUser = (SessionUserDTO) StpUtil.getSessionByLoginId(loginId).get(AuthConstant.USER_SESSION_KEY);
        return Collections.singletonList(UserRoleEnums.getRoleByCode(currentUser.getUserRole()));
    }

}

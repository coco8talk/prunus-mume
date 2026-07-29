package com.coco8talk.pm.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.user.model.dto.*;
import com.coco8talk.pm.user.model.entity.User;
import com.coco8talk.pm.user.model.vo.LoginUserVO;
import com.coco8talk.pm.user.model.vo.UserForAdminVO;
import com.coco8talk.pm.user.model.vo.UserVO;
import jakarta.validation.Valid;

/**
 * 用户service层
 *
 * @author coco8talk
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-06-22 23:21:20
 */
public interface UserService extends IService<User> {

    /**
     * 管理员创建用户（管理员）
     *
     * @param createUserDTO 创建的用户信息
     * @return 携带用户 id 的响应结果
     */
    Result<Long> adminCreateUser(@Valid CreateUserDTO createUserDTO);
    
    /**
     * 管理员根据用户id删除用户（管理员）
     *
     * @param deleteUserDTO 包含用户Id的请求参数封装类
     * @return 删除结果 成功-true
     */
    Result<Void> adminDeleteUser(@Valid DeleteUserDTO deleteUserDTO);
    
    /**
     * 管理员更新用户信息
     *
     * @param adminEditUserDTO 更新的内容参数封装类
     * @return 更新结果
     */
    Result<Void> adminEditUser(AdminEditUserDTO adminEditUserDTO);
    
    /**
     * 编辑用户信息
     *
     * @param editSelfDTO 编辑参数
     * @return 编辑结果 true-成功
     */
    Result<Void> userEditSelf(EditSelfDTO editSelfDTO);
    
    /**
     * 用户编辑自己的用户密码
     *
     * @param editOwnPasswordDTO 参数封装类，包含新的用户密码
     * @return 如有错误抛出异常
     */
    Result<Void> userEditOwnPassword(@Valid EditOwnPasswordDTO editOwnPasswordDTO);
    
    /**
     * 根据用户id查询用户详细信息（管理员）
     *
     * @param userId 查询用户的Id
     * @return 用户详细信息
     */
    Result<UserForAdminVO> adminQueryUserById(Long userId);
    
    /**
     * 根据条件分页查询用户（仅管理员）
     *
     * @param queryUserDTO 查询条件
     * @return 查询结果
     */
    Result<Page<UserForAdminVO>> adminQueryUserPage(QueryUserDTO queryUserDTO);
    
    /**
     * 根据用户id查询用户脱敏信息
     *
     * @param userId 查询用户的Id
     * @return 用户脱敏信息
     */
    Result<UserVO> queryUserById(Long userId);

    /**
     * 根据用户id查询用户信息，由调用者身份决定返回完整字段（管理员）还是脱敏字段（其他人）
     *
     * @param userId 查询用户的Id
     * @return 用户信息，管理员为 UserForAdminVO，其他人为 UserVO
     */
    Result<Object> queryUserByIdForCaller(Long userId);

    /**
     * 根据条件分页查询用户脱敏信息
     *
     * @param queryUserDTO 查询条件
     * @return 查询结果
     */
    Result<Page<UserVO>> queryUserPage(QueryUserDTO queryUserDTO);

    /** 取当前登录用户脱敏信息（LoginUserVO）。未登录由 AuthSessionApi 抛业务异常。 */
    Result<LoginUserVO> getCurrentLoginUser();
}


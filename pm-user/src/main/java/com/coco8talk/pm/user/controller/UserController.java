package com.coco8talk.pm.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.util.IdUtils;
import com.coco8talk.pm.common.util.ObjectMyUtil;
import com.coco8talk.pm.user.common.constant.UserConstant;
import com.coco8talk.pm.user.model.dto.*;
import com.coco8talk.pm.user.model.vo.LoginUserVO;
import com.coco8talk.pm.user.model.vo.UserForAdminVO;
import com.coco8talk.pm.user.model.vo.UserVO;
import com.coco8talk.pm.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制类
 *
 * @author coco8talk
 * @since 2025/6/23 0:55
 */
@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name = "用户相关接口", description = "提供个人资料维护以及管理员用户管理能力（注册/登录/登出已迁移至 pm-auth）")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 管理员创建用户（管理员）
     *
     * @param createUserDTO 创建的用户信息
     * @return 携带用户 id 的响应结果
     */
    @PostMapping
    @Operation(summary = "添加用户（管理员）", description = "由管理员创建用户账号，未指定密码时使用系统默认密码")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Long> adminCreateUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        log.info("管理员创建用户请求: {}", createUserDTO);
        return userService.adminCreateUser(createUserDTO);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户（管理员）", description = "由管理员删除指定用户并同步清理该用户的会话及关联数据")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Void> adminDeleteUser(@PathVariable("userId") String userIdStr) {
        Long userId = IdUtils.parseId(userIdStr, "用户ID");
        log.info("管理员删除用户请求: userId={}", userId);
        DeleteUserDTO deleteUserDTO = new DeleteUserDTO();
        deleteUserDTO.setId(userId);
        return userService.adminDeleteUser(deleteUserDTO);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户（管理员）", description = "由管理员修改指定用户的名称、角色、简介或头像等资料")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Void> adminEditUser(@PathVariable("userId") String userIdStr,
                                       @RequestBody @Valid AdminEditUserDTO adminEditUserDTO) {
        log.info("管理员编辑用户请求: {}", adminEditUserDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(adminEditUserDTO, HttpStatusEnum.BAD_REQUEST, "请输入修改信息");
        Long userId = IdUtils.parseId(userIdStr, "用户ID");
        adminEditUserDTO.setId(userId);
        return userService.adminEditUser(adminEditUserDTO);
    }
    
    /**
     * 编辑用户自己的信息
     *
     * @param editSelfDTO 需要编辑的信息
     * @return 编辑结果 true-成功
     */
    @PutMapping("/me")
    @Operation(summary = "编辑用户", description = "修改当前登录用户的个人资料并同步刷新会话信息")
    @SaCheckLogin
    public Result<Void> userEditSelf(@RequestBody @Valid EditSelfDTO editSelfDTO) {
        log.info("用户编辑自己的信息请求: {}", editSelfDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(editSelfDTO, HttpStatusEnum.BAD_REQUEST, "请正确输入需要修改的信息");
        return userService.userEditSelf(editSelfDTO);
    }
    
    /**
     * 用户编辑自己的用户密码
     *
     * @param editOwnPasswordDTO 参数封装类，包含新的用户密码
     * @return 如有错误抛出异常
     */
    @PutMapping("/me/password")
    @Operation(summary = "修改密码", description = "校验旧密码后修改当前登录用户的账号密码")
    @SaCheckLogin
    public Result<Void> userEditOwnPassword(@RequestBody @Valid EditOwnPasswordDTO editOwnPasswordDTO) {
        log.info("用户编辑自己的密码请求: {}", editOwnPasswordDTO);
        return userService.userEditOwnPassword(editOwnPasswordDTO);
    }
    
    /**
     * 获取当前登录用户的脱敏信息
     *
     * @return 当前登录用户 LoginUserVO
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户", description = "读取当前会话对应用户的脱敏资料")
    @SaCheckLogin
    public Result<LoginUserVO> currentUser() {
        return userService.getCurrentLoginUser();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户", description = "按用户 ID 查询用户资料；管理员可见完整字段，其他调用者仅可见脱敏字段")
    public Result<Object> queryUserById(@PathVariable("userId") String userIdStr) {
        Long userId = IdUtils.parseId(userIdStr, "用户ID");
        log.info("查询用户信息请求: userId={}", userId);
        return userService.queryUserByIdForCaller(userId);
    }

    /**
     * 根据条件分页查询用户，管理员返回完整字段，其他调用者返回脱敏字段
     *
     * @param queryUserDTO 查询条件
     * @return 查询结果
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询", description = "按条件分页查询用户；管理员返回完整字段，其他调用者返回脱敏字段")
    public Result<Object> queryUserPage(@RequestBody QueryUserDTO queryUserDTO) {
        log.info("分页查询用户请求: {}", queryUserDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(queryUserDTO, HttpStatusEnum.BAD_REQUEST, "请输入合法的查询条件");
        return userService.queryUserPageForCaller(queryUserDTO);
    }
}

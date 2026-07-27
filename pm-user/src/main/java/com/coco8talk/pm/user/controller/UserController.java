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
@RequestMapping("/user")
@Tag(name = "用户相关接口", description = "提供用户注册登录、个人资料维护以及管理员用户管理能力")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 用户注册功能
     *
     * @param registerUserDTO 用户注册信息
     * @return 用户 id
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "校验注册信息并创建新用户账号，返回生成的用户 ID")
    public Result<Long> userRegister(@RequestBody @Valid RegisterUserDTO registerUserDTO) {
        log.info("用户注册请求: {}", registerUserDTO);
        return userService.userRegister(registerUserDTO);
    }
    
    /**
     * 用户登录功能
     *
     * @param loginUserDTO 用户登录信息
     * @return 脱敏用户信息（LoginUserVO）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验账号密码并建立登录会话，返回当前用户的脱敏信息")
    public Result<LoginUserVO> userLogin(@RequestBody @Valid LoginUserDTO loginUserDTO) {
        log.info("用户登录请求: {}", loginUserDTO);
        return userService.userLogin(loginUserDTO);
    }
    
    /**
     * 用户退出登录态
     *
     * @return 处理结果
     */
    @PostMapping("/logout")
    @Operation(summary = "注销用户", description = "注销当前用户会话并清理登录状态")
    public Result<Void> userLogout() {
        log.info("用户注销请求");
        return userService.userLogout();
    }
    
    /**
     * 管理员创建用户（管理员）
     *
     * @param createUserDTO 创建的用户信息
     * @return 携带用户 id 的响应结果
     */
    @PostMapping("/admin")
    @Operation(summary = "添加用户（管理员）", description = "由管理员创建用户账号，未指定密码时使用系统默认密码")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Long> adminCreateUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        log.info("管理员创建用户请求: {}", createUserDTO);
        return userService.adminCreateUser(createUserDTO);
    }
    
    /**
     * 管理员根据用户id删除用户（管理员）
     *
     * @param deleteUserDTO 包含用户Id的请求参数封装类
     * @return 删除结果 成功-true
     */
    @DeleteMapping("/admin")
    @Operation(summary = "删除用户（管理员）", description = "由管理员删除指定用户并同步清理该用户的会话及关联数据")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Void> adminDeleteUser(@RequestBody @Valid DeleteUserDTO deleteUserDTO) {
        log.info("管理员删除用户请求: {}", deleteUserDTO);
        return userService.adminDeleteUser(deleteUserDTO);
    }
    
    /**
     * 管理员更新用户信息（管理员）
     *
     * @param adminEditUserDTO 更新的内容参数封装类
     * @return 更新结果
     */
    @PutMapping("/admin")
    @Operation(summary = "更新用户（管理员）", description = "由管理员修改指定用户的名称、角色、简介或头像等资料")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Void> adminEditUser(@RequestBody @Valid AdminEditUserDTO adminEditUserDTO) {
        log.info("管理员编辑用户请求: {}", adminEditUserDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(adminEditUserDTO, HttpStatusEnum.BAD_REQUEST, "请输入修改信息");
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
     * 根据用户id查询用户详细信息（仅管理员）
     *
     * @param userIdStr 查询用户的Id（字符串形式，避免大数精度丢失）
     * @return 用户详细信息
     */
    @GetMapping("/admin/{userId}")
    @Operation(summary = "获取用户（管理员）", description = "由管理员按用户 ID 查询包含管理字段的完整用户信息")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<UserForAdminVO> adminQueryUserById(@PathVariable("userId") String userIdStr) {
        Long userId = IdUtils.parseId(userIdStr, "用户ID");
        log.info("管理员查询用户信息请求: userId={}", userId);
        return userService.adminQueryUserById(userId);
    }
    
    /**
     * 根据条件分页查询用户（仅管理员）
     *
     * @param queryUserDTO 查询条件
     * @return 查询结果
     */
    @PostMapping("/admin/search")
    @Operation(summary = "分页查询（管理员）", description = "由管理员按账号、名称、角色等条件分页查询用户")
    @SaCheckRole(UserConstant.ADMIN_USER_ROLE)
    public Result<Page<UserForAdminVO>> adminQueryUserPage(@RequestBody @Valid QueryUserDTO queryUserDTO) {
        log.info("管理员分页查询用户请求: {}", queryUserDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(queryUserDTO, HttpStatusEnum.BAD_REQUEST, "请正确输入查询条件");
        return userService.adminQueryUserPage(queryUserDTO);
    }
    
    /**
     * 根据用户id查询用户脱敏信息
     *
     * @param userIdStr 查询用户的Id（字符串形式，避免大数精度丢失）
     * @return 用户脱敏信息
     */

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
    @Operation(summary = "获取用户", description = "按用户 ID 查询可公开展示的脱敏用户资料")
    public Result<UserVO> queryUserById(@PathVariable("userId") String userIdStr) {
        Long userId = IdUtils.parseId(userIdStr, "用户ID");
        log.info("查询用户信息请求: userId={}", userId);
        return userService.queryUserById(userId);
    }
    
    /**
     * 根据条件分页查询用户脱敏信息
     *
     * @param queryUserDTO 查询条件
     * @return 查询结果
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询", description = "按公开查询条件分页获取脱敏用户列表")
    public Result<Page<UserVO>> queryUserPage(@RequestBody QueryUserDTO queryUserDTO) {
        log.info("分页查询用户请求: {}", queryUserDTO);
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(queryUserDTO, HttpStatusEnum.BAD_REQUEST, "请输入合法的查询条件");
        return userService.queryUserPage(queryUserDTO);
    }
}

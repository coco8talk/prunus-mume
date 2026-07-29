package com.coco8talk.pm.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.api.auth.service.AuthSessionApi;
import com.coco8talk.pm.api.auth.service.CurrentUserProvider;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.api.auth.enums.UserRoleEnums;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.common.util.DtoValidationUtil;
import com.coco8talk.pm.common.util.ObjectMyUtil;
import com.coco8talk.pm.common.util.QueryWrapperUtil;
import com.coco8talk.pm.user.common.constant.UserConstant;
import com.coco8talk.pm.api.user.service.UserDeletionCleaner;
import com.coco8talk.pm.user.convert.UserMapstruct;
import com.coco8talk.pm.user.mapper.UserMapper;
import com.coco8talk.pm.user.model.dto.*;
import com.coco8talk.pm.user.model.entity.User;
import com.coco8talk.pm.user.model.vo.LoginUserVO;
import com.coco8talk.pm.user.model.vo.UserForAdminVO;
import com.coco8talk.pm.user.model.vo.UserVO;
import com.coco8talk.pm.user.service.UserService;
import com.coco8talk.pm.user.service.support.UserAccountSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.regex.Pattern;

/**
 * 用户服务实现类
 *
 * @author coco8talk
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-06-22 23:21:20
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final UserAccountSupport userAccountSupport;
    private final AuthSessionApi authSessionApi;
    private final CurrentUserProvider currentUserProvider;
    private final java.util.List<UserDeletionCleaner> deletionCleaners;

    public UserServiceImpl(UserAccountSupport userAccountSupport, AuthSessionApi authSessionApi,
                            CurrentUserProvider currentUserProvider, java.util.List<UserDeletionCleaner> deletionCleaners) {
        this.userAccountSupport = userAccountSupport;
        this.authSessionApi = authSessionApi;
        this.currentUserProvider = currentUserProvider;
        this.deletionCleaners = deletionCleaners;
    }

    // ========== 管理员接口 ==========
    
    /**
     * 创建用户（管理员）
     */
    @Override
    public Result<Long> adminCreateUser(CreateUserDTO createUserDTO) {
        String userAccount = createUserDTO.getUserAccount();

        // 参数校验
        userAccountSupport.validateUserAccountFormat(userAccount);
        validateUserNameFormat(createUserDTO.getUserName());
        validateUserRoleFormat(createUserDTO.getUserRole());
        userAccountSupport.validateUserAccountExists(userAccount, true);

        return userAccountSupport.executeWithUserAccountLock(userAccount, () -> {
            // 再次检查账号唯一性
            userAccountSupport.validateUserAccountExists(userAccount, true);

            // 创建默认密码并加密
            String encryptedPassword = userAccountSupport.encryptPassword(UserConstant.USER_DEFAULT_PASSWORD);
            
            // 保存用户
            User userToInsert = UserMapstruct.INSTANCE.addDtoToEntity(createUserDTO);
            userToInsert.setUserPassword(encryptedPassword);
            if (userToInsert.getUserRole() == null) {
                userToInsert.setUserRole(UserRoleEnums.NORMAL.getCode());
            }
            boolean saveResult = this.save(userToInsert);
            ThrowUtils.throwIfFalse(saveResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "数据库写入失败");
            
            return Result.success(HttpStatusEnum.OK, userToInsert.getId());
        });
    }
    
    /**
     * 删除用户（管理员）
     */
    @Override
    public Result<Void> adminDeleteUser(DeleteUserDTO deleteUserDTO) {
        // 检查用户是否存在
        DtoValidationUtil.validateEntityExists(deleteUserDTO.getId(), this, "用户");
        
        // 清除会话信息
        authSessionApi.logoutById(deleteUserDTO.getId());
        
        // 删除用户
        boolean deleteResult = this.removeById(deleteUserDTO);
        Long userId = deleteUserDTO.getId();
        deletionCleaners.forEach(c -> c.onUserDeleted(userId));
        ThrowUtils.throwIfNull(deleteResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "从数据库删除失败");
        
        return Result.success(HttpStatusEnum.NO_CONTENT);
    }
    
    /**
     * 编辑用户（管理员）
     */
    @Override
    public Result<Void> adminEditUser(AdminEditUserDTO adminEditUserDTO) {
        // 参数校验
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(adminEditUserDTO, HttpStatusEnum.BAD_REQUEST, "请正确输入需要修改的信息");
        String newUserName = adminEditUserDTO.getUserName();
        validateUserNameFormat(newUserName);
        Integer newUserRole = adminEditUserDTO.getUserRole();
        validateUserRoleFormat(newUserRole);
        String newUserProfile = adminEditUserDTO.getUserProfile();
        validateUserProfileFormat(newUserProfile);
        String newUserAvatar = adminEditUserDTO.getUserAvatar();

        boolean isNeedToUpdate = true;
        
        // 检查用户是否存在
        Long userId = adminEditUserDTO.getId();
        User oldUser = DtoValidationUtil.validateEntityExists(userId, this, "用户");
        
        // 检查新旧信息是否一致
        if (newUserName != null && newUserName.equals(oldUser.getUserName())) {
            adminEditUserDTO.setUserName(null);
            isNeedToUpdate = false;
        }
        if (newUserRole != null && newUserRole.equals(oldUser.getUserRole())) {
            adminEditUserDTO.setUserRole(null);
            isNeedToUpdate = false;
        }
        if (newUserProfile != null && newUserProfile.equals(oldUser.getUserProfile())) {
            adminEditUserDTO.setUserProfile(null);
            isNeedToUpdate = false;
        }
        if (newUserAvatar != null && newUserAvatar.equals(oldUser.getUserAvatar())) {
            adminEditUserDTO.setUserProfile(null);
            isNeedToUpdate = false;
        }

        // 更新用户
        if(isNeedToUpdate) {
            User userToDb = UserMapstruct.INSTANCE.adminEditDtoToEntity(adminEditUserDTO);
            boolean updateResult = this.updateById(userToDb);
            ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新数据库失败");

            // 更新会话信息
            Long editUserId = adminEditUserDTO.getId();
            if (authSessionApi.isCurrentUser(editUserId)) {
                User newUser = this.getById(editUserId);
                SessionUserDTO sessionUserDTO = UserMapstruct.INSTANCE.entityToSessionDto(newUser);
                authSessionApi.updateSession(sessionUserDTO);
            }
        }
        
        return Result.success(HttpStatusEnum.NO_CONTENT);
    }
    
    // ========== 用户接口 ==========
    
    /**
     * 编辑用户信息（自己）
     */
    @Override
    public Result<Void> userEditSelf(EditSelfDTO editSelfDTO) {
        // 参数校验
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(editSelfDTO, HttpStatusEnum.BAD_REQUEST, "请正确输入需要修改的信息");
        validateUserNameFormat(editSelfDTO.getUserName());
        validateUserProfileFormat(editSelfDTO.getUserProfile());
        validateUserPhoneNumberFormat(editSelfDTO.getPhoneNumber());
        validatedUserEmailFormat(editSelfDTO.getEmail());
        validateUserGradeFormat(editSelfDTO.getGrade());
        validateUserWorkExperience(editSelfDTO.getWorkExperience());
        validateUserExpertiseDirection(editSelfDTO.getExpertiseDirection());
        
        // 更新用户信息
        User userToDb = UserMapstruct.INSTANCE.editDtoToEntity(editSelfDTO);
        Long currentUserId = authSessionApi.currentSession().getId();
        userToDb.setId(currentUserId);
        boolean updateResult = this.updateById(userToDb);
        ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新数据库失败");
        
        // 更新会话信息
        User newUser = this.getById(currentUserId);
        SessionUserDTO sessionUserDTO = UserMapstruct.INSTANCE.entityToSessionDto(newUser);
        authSessionApi.updateSession(sessionUserDTO);

        return Result.success(HttpStatusEnum.NO_CONTENT);
    }
    
    /**
     * 修改密码（自己）
     */
    @Override
    public Result<Void> userEditOwnPassword(EditOwnPasswordDTO editOwnPasswordDTO) {
        // 参数校验
        userAccountSupport.validateUserPasswordFormat(editOwnPasswordDTO.getPassword());
        userAccountSupport.validateUserPasswordFormat(editOwnPasswordDTO.getCheckPassword());

        String newPassword = editOwnPasswordDTO.getPassword();
        ThrowUtils.throwIfFalse(
            newPassword.equals(editOwnPasswordDTO.getCheckPassword()),
            HttpStatusEnum.BAD_REQUEST,
            "两次密码输入不一致");

        // 获取当前用户
        SessionUserDTO currentUserSession = authSessionApi.currentSession();
        User currentUser = this.getById(currentUserSession.getId());
        ThrowUtils.throwIfNull(currentUser, HttpStatusEnum.BAD_REQUEST, "数据库中不存在当前用户信息");
        String userPassword = currentUser.getUserPassword();

        // 检查新旧密码是否一致
        boolean isSamePassword = userAccountSupport.verifyPassword(newPassword, userPassword);
        ThrowUtils.throwIfTrue(isSamePassword, HttpStatusEnum.BAD_REQUEST, "新密码与旧密码不可相同");

        // 密码加密
        String encryptedPassword = userAccountSupport.encryptPassword(newPassword);
        
        // 更新密码
        User userToDb = new User();
        userToDb.setId(currentUser.getId());
        userToDb.setUserPassword(encryptedPassword);
        
        boolean updateResult = this.updateById(userToDb);
        ThrowUtils.throwIfFalse(updateResult, HttpStatusEnum.INTERNAL_SERVER_ERROR, "更新数据库失败");
        
        return Result.success(HttpStatusEnum.NO_CONTENT);
    }
    
    // ========== 查询接口 ==========
    
    /**
     * 根据ID查询用户（管理员）
     */
    @Override
    public Result<UserForAdminVO> adminQueryUserById(Long userId) {
        // 检查用户是否存在
        User userFromDb = DtoValidationUtil.validateEntityExists(userId, this, "用户");
        
        // 转换为VO
        UserForAdminVO userForAdminVO = UserMapstruct.INSTANCE.entityToForAdminVo(userFromDb);
        return Result.success(HttpStatusEnum.OK, userForAdminVO);
    }
    
    /**
     * 根据ID查询用户
     */
    @Override
    public Result<UserVO> queryUserById(Long userId) {
        // 检查用户是否存在
        User userFromDb = DtoValidationUtil.validateEntityExists(userId, this, "用户");

        // 转换为VO
        UserVO userVO = UserMapstruct.INSTANCE.entityToVo(userFromDb);
        return Result.success(HttpStatusEnum.OK, userVO);
    }

    /**
     * 根据ID查询用户，按调用者身份返回完整字段或脱敏字段
     */
    @Override
    public Result<Object> queryUserByIdForCaller(Long userId) {
        boolean callerIsAdmin = currentUserProvider.isLoggedIn() && currentUserProvider.isAdmin();
        if (callerIsAdmin) {
            return castToObjectResult(this.adminQueryUserById(userId));
        }
        return castToObjectResult(this.queryUserById(userId));
    }

    /**
     * 分页查询用户（管理员）
     */
    @Override
    public Result<Page<UserForAdminVO>> adminQueryUserPage(QueryUserDTO queryUserDTO) {
        // 参数校验
        DtoValidationUtil.validatePageCurrentFormat(queryUserDTO.getCurrent());
        DtoValidationUtil.validatePageSizeFormat(queryUserDTO.getPageSize());
        
        // 创建查询条件
        LambdaQueryWrapper<User> wrapper = createQueryUserWrapper(queryUserDTO);
        
        // 执行查询
        Page<User> userPage = this.page(new Page<>(queryUserDTO.getCurrent(), queryUserDTO.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(userPage.getRecords())) {
            return Result.fail(HttpStatusEnum.BAD_REQUEST, "根据当前条件未查询出用户");
        }
        
        // 转换为VO
        Page<UserForAdminVO> userForAdminVoPage = UserMapstruct.INSTANCE.entityPageToForAdminVoPage(userPage);
        return Result.success(HttpStatusEnum.OK, userForAdminVoPage);
    }
    
    /**
     * 分页查询用户
     */
    @Override
    public Result<Page<UserVO>> queryUserPage(QueryUserDTO queryUserDTO) {
        // 调用管理员查询方法
        Result<Page<UserForAdminVO>> pageResult = this.adminQueryUserPage(queryUserDTO);
        Page<UserForAdminVO> adminQueryPage = pageResult.getData();
        // 将管理员查询结果转换为普通用户VO
        Page<UserVO> userVoPage = UserMapstruct.INSTANCE.entityPageToVoPage(adminQueryPage);
        return Result.success(HttpStatusEnum.OK, userVoPage);
    }

    @Override
    public Result<LoginUserVO> getCurrentLoginUser() {
        Long currentUserId = authSessionApi.currentSession().getId();
        User user = this.getById(currentUserId);
        ThrowUtils.throwIfNull(
            user, HttpStatusEnum.UNAUTHORIZED, "登录态无效");
        LoginUserVO vo = UserMapstruct.INSTANCE.entityToLoginVo(user);
        return Result.success(HttpStatusEnum.OK, vo);
    }
    
    // ========== 私有方法 ==========

    /**
     * Result<T> 在运行时被擦除为 Result<Object>；这里只读取 code/data/message，从不依赖静态类型 T，
     * 因此该 unchecked cast 是安全的——用它代替重新拼装 Result，避免丢失 fail()/error() 的 code。
     */
    @SuppressWarnings("unchecked")
    private static <S> Result<Object> castToObjectResult(Result<S> source) {
        return (Result<Object>) (Result<?>) source;
    }

    /**
     * 创建查询条件
     */
    private LambdaQueryWrapper<User> createQueryUserWrapper(QueryUserDTO queryUserDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 根据用户ID查询
        Long userId = queryUserDTO.getId();
        if (userId != null) {
            wrapper.eq(User::getId, userId);
        }
        
        // 根据用户名称查询
        String userName = queryUserDTO.getUserName();
        if (StringUtils.isNotBlank(userName)) {
            wrapper.like(User::getUserName, userName);
        }
        
        // 根据用户权限查询
        Integer userRole = queryUserDTO.getUserRole();
        if (userRole != null) {
            wrapper.eq(User::getUserRole, userRole);
        }
        
        // 应用排序条件
        
        return QueryWrapperUtil.applySorting(wrapper, queryUserDTO.getSortField(), queryUserDTO.getSortOrder());
    }
    
    // ========== 校验方法 ==========

    /**
     * 验证用户名格式
     */
    private void validateUserNameFormat(String userName) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(userName) && userName.length() > UserConstant.USER_NAME_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "用户名长度不能超过" + UserConstant.USER_NAME_MAX_LENGTH + "个字符");
    }
    
    /**
     * 验证用户权限格式
     */
    private void validateUserRoleFormat(Integer userRole) {
        if (userRole == null) {
            return;
        }
        
        boolean isValidRole = userRole.equals(UserRoleEnums.ADMIN.getCode()) ||
            userRole.equals(UserRoleEnums.NORMAL.getCode()) ||
            userRole.equals(UserRoleEnums.VIP.getCode());
        ThrowUtils.throwIfFalse(isValidRole, HttpStatusEnum.BAD_REQUEST, "用户权限不合法");
    }
    
    /**
     * 验证用户简介格式
     */
    private void validateUserProfileFormat(String profile) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(profile) && profile.length() > UserConstant.USER_PROFILE_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "用户简介长度不能超过" + UserConstant.USER_PROFILE_MAX_LENGTH + "个字符");
    }
    
    /**
     * 验证用户手机号格式
     */
    private void validateUserPhoneNumberFormat(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return;
        }
        
        Pattern pattern = Pattern.compile(UserConstant.USER_PHONE_FORMAT_REGEX);
        ThrowUtils.throwIfFalse(pattern.matcher(phoneNumber).matches(),
            HttpStatusEnum.BAD_REQUEST, "手机号格式不正确");
    }
    
    /**
     * 验证用户邮箱格式
     */
    private void validatedUserEmailFormat(String email) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        
        Pattern pattern = Pattern.compile(UserConstant.USER_EMAIL_FORMAT_REGEX);
        ThrowUtils.throwIfFalse(pattern.matcher(email).matches(),
            HttpStatusEnum.BAD_REQUEST, "邮箱格式不正确");
    }
    
    /**
     * 验证用户年级格式
     */
    private void validateUserGradeFormat(Integer grade) {
        ThrowUtils.throwIfTrue(grade != null && (grade < UserConstant.USER_GRADE_MIN_VALUE || grade > UserConstant.USER_GRADE_MAX_VALUE),
            HttpStatusEnum.BAD_REQUEST, "用户年级必须在" + UserConstant.USER_GRADE_MIN_VALUE + "-" + UserConstant.USER_GRADE_MAX_VALUE + "之间");
    }
    
    /**
     * 验证用户工作经验格式
     */
    private void validateUserWorkExperience(String workExperience) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(workExperience) && workExperience.length() > UserConstant.USER_WORK_EXPERIENCE_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "工作经验长度不能超过" + UserConstant.USER_WORK_EXPERIENCE_MAX_LENGTH + "个字符");
    }
    
    /**
     * 验证用户专业方向格式
     */
    private void validateUserExpertiseDirection(String expertiseDirection) {
        ThrowUtils.throwIfTrue(StringUtils.isNotBlank(expertiseDirection) && expertiseDirection.length() > UserConstant.USER_EXPERTISE_DIRECTION_MAX_LENGTH,
            HttpStatusEnum.BAD_REQUEST, "专业方向长度不能超过" + UserConstant.USER_EXPERTISE_DIRECTION_MAX_LENGTH + "个字符");
    }
}





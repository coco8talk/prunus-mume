package com.coco8talk.pm.user.model.dto;

import com.coco8talk.pm.user.common.constant.UserConstant;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 编辑用户信息参数封装类
 *
 * @author coco8talk
 * @since 2025/6/26 17:45
 **/
@Data
public class EditSelfDTO {
    /**
     * 用户名称
     */
    @Size(max = UserConstant.USER_NAME_MAX_LENGTH,
        message = "用户名长度应在" + UserConstant.USER_NAME_MIN_LENGTH + "-" + UserConstant.USER_NAME_MAX_LENGTH + "个字符之间")
    private String userName;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 用户简介
     */
    @Size(max = UserConstant.USER_PROFILE_MAX_LENGTH,
        message = "用户简介长度不可以超过" + UserConstant.USER_PROFILE_MAX_LENGTH + "个字符")
    private String userProfile;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^\\d{11}$", message = "电话号码格式不正确")
    private String phoneNumber;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 年级（1-小学 2-初中 3-高中 4-大一 5-大二 6-大三 7-大四 8-硕士 9-博士）
     */
    @Min(value = UserConstant.USER_GRADE_MIN_VALUE, message = "请输入合法的年级信息")
    @Max(value = UserConstant.USER_GRADE_MAX_VALUE, message = "请输入合法的年级信息")
    private Integer grade;
    
    /**
     * 工作经验
     */
    @Size(max = UserConstant.USER_WORK_EXPERIENCE_MAX_LENGTH,
        message = "工作经验长度不可超过" + UserConstant.USER_WORK_EXPERIENCE_MAX_LENGTH + "个字符")
    private String workExperience;
    
    /**
     * 擅长方向
     */
    @Size(max = UserConstant.USER_EXPERTISE_DIRECTION_MAX_LENGTH,
        message = "工作擅长方向不可超过" + UserConstant.USER_EXPERTISE_DIRECTION_MAX_LENGTH + "个字符")
    private String expertiseDirection;
}

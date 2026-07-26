package com.coco8talk.pm.user.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.user.model.dto.AdminEditUserDTO;
import com.coco8talk.pm.user.model.dto.CreateUserDTO;
import com.coco8talk.pm.user.model.dto.EditSelfDTO;
import com.coco8talk.pm.user.model.entity.User;
import com.coco8talk.pm.user.model.vo.LoginUserVO;
import com.coco8talk.pm.user.model.vo.UserForAdminVO;
import com.coco8talk.pm.user.model.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户对象映射转换接口
 *
 * @author coco8talk
 * @since 2025/6/23 19:53
 **/
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapstruct {
    UserMapstruct INSTANCE = Mappers.getMapper(UserMapstruct.class);
    
    /**
     * 将 User 对象 转换为 LoginUserVO
     *
     * @param user 待转换的 User 对象
     * @return 转换完成的 LoginUserVO 对象
     */
    LoginUserVO entityToLoginVo(User user);
    
    /**
     * 将 User 对象转换为 SessionUserDTO
     *
     * @param user 待转换的 User 对象
     * @return 转换完成的SessionUserDTO对象
     */
    SessionUserDTO entityToSessionDto(User user);
    
    /**
     * 将 User 对象转换为 UserVO
     *
     * @param user 待转换的 User 对象
     * @return 转换完成的UserVO对象
     */
    UserVO entityToVo(User user);
    
    /**
     * 将 User 对象列表转换为 UserVO 列表
     *
     * @param userList 待转换的 User 对象列表
     * @return 转换完成的 UserVO 列表
     */
    List<UserVO> entityListToVoList(List<User> userList);
    
    /**
     * 将 User 对象转换为 UserForAdminVO
     *
     * @param getById 待转换的 User 对象
     * @return 转换完成的UserForAdminVO对象
     */
    UserForAdminVO entityToForAdminVo(User getById);
    
    /**
     * 将 Page(UserForAdminVO) 对象转换成 Page(UserVO)
     *
     * @param adminQueryPage 待转换的 Page(UserForAdminVO)
     * @return 转换完成的 Page(UserVO) 对象
     */
    Page<UserVO> entityPageToVoPage(Page<UserForAdminVO> adminQueryPage);
    
    /**
     * 将 Page(User) 对象转换成 Page(UserForAdminVO)
     *
     * @param userPage 待转换的 Page(User)
     * @return 转换完成的 Page(UserForAdminVO) 对象
     */
    Page<UserForAdminVO> entityPageToForAdminVoPage(Page<User> userPage);
    
    /**
     * 将 AddUserDTO 对象转换为 User
     *
     * @param createUserDTO 待转换的 AddUserDTO 对象
     * @return 转换完成的User对象
     */
    @Mapping(target = "userRole", ignore = true)
    User addDtoToEntity(CreateUserDTO createUserDTO);
    
    /**
     * 将 UpdateUserDTO 对象转换为 User
     *
     * @param adminEditUserDTO 待转换的 UpdateUserDTO 对象
     * @return 转换完成的User对象
     */
    User adminEditDtoToEntity(AdminEditUserDTO adminEditUserDTO);
    
    /**
     * 将 EditUserDTO 对象转换为 User
     *
     * @param editSelfDTO 待转换的 EditUserDTO 对象
     * @return 转换完成的User对象
     */
    User editDtoToEntity(EditSelfDTO editSelfDTO);
}

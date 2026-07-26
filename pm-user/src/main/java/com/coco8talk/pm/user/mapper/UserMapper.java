package com.coco8talk.pm.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coco8talk.pm.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据库操作Mapper
 *
 * @author coco8talk
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2025-06-22 23:21:20
 * @Entity com.coco8talk.pm.user.model.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    int updateAvatarById(String avatarUrl, String avatarCosKey);
}





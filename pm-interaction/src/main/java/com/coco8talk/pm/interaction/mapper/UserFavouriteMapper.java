package com.coco8talk.pm.interaction.mapper;
import com.coco8talk.pm.interaction.model.entity.UserFavourite;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户收藏表数据库操作Mapper
 *
 * @author coco8talk
 * @description 针对表【user_favourite(用户收藏表)】的数据库操作Mapper
 * @createDate 2025-06-22 23:21:23
 * @Entity com.coco8talk.pm.interaction.model.entity.UserFavourite
 */
@Mapper
public interface UserFavouriteMapper extends BaseMapper<UserFavourite> {
    /**
     * 查询用户收藏的题目ID列表
     *
     * @param existsQuestionIds 题目ID列表
    * @return 存在的题目ID列表
    */
    @Select("<script>" +
            "SELECT id FROM user_favourite WHERE question_id IN " +
            "<foreach collection='existsQuestionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectExistsFavouritesByQuestionIds(List<Long> existsQuestionIds);
}




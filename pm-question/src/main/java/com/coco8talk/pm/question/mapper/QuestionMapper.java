package com.coco8talk.pm.question.mapper;
import com.coco8talk.pm.question.model.entity.Question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 题目表数据库操作Mapper
 *
 * @author coco8talk
 * @description 针对表【question(题目表)】的数据库操作Mapper
 * @createDate 2025-06-22 23:21:09
 * @Entity com.coco8talk.pm.question.model.entity.Question
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    /**
     * 查询题目Id列表，判断题目是否存在
     *
     * @param uniqueQuestionIds 题目Id列表
     * @return 存在的题目Id列表
    */
    @Select("<script>" +
            "select id from question where id in " +
            "<foreach collection='uniqueQuestionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectExistsIds(List<Long> uniqueQuestionIds);

    /**
     * 调整题目点赞数（thumb_num += delta，下限为 0）。
     *
     * @param questionId 题目ID
     * @param delta      增量（可正可负）
     * @return 受影响行数
     */
    @Update("update question set thumb_num = greatest(thumb_num + #{delta}, 0) where id = #{questionId}")
    int incrementThumbNum(@Param("questionId") Long questionId, @Param("delta") int delta);

    /**
     * 调整题目收藏数（favour_num += delta，下限为 0）。
     *
     * @param questionId 题目ID
     * @param delta      增量（可正可负）
     * @return 受影响行数
     */
    @Update("update question set favour_num = greatest(favour_num + #{delta}, 0) where id = #{questionId}")
    int incrementFavourNum(@Param("questionId") Long questionId, @Param("delta") int delta);

    /**
     * 原子自增题目浏览量(view_num += 1)。用 SQL 直接自增,避免读改写的丢失更新,
     * 并避免在缓存命中的详情读路径上额外触发 {@code selectById} 回库。
     *
     * @param questionId 题目ID
     * @return 受影响行数
     */
    @Update("update question set view_num = ifnull(view_num, 0) + 1 where id = #{questionId}")
    int incrementViewNum(@Param("questionId") Long questionId);
}




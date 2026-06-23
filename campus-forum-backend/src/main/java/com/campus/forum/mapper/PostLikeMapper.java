package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.PostLike;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    @Select("SELECT COUNT(*) FROM post_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int checkLiked(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    @Delete("DELETE FROM post_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int deleteLike(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);
}

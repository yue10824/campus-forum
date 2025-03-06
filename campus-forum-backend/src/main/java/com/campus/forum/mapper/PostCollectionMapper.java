package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.PostCollection;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PostCollectionMapper extends BaseMapper<PostCollection> {

    @Select("SELECT COUNT(*) FROM post_collection WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int checkCollected(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    @Delete("DELETE FROM post_collection WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int deleteCollection(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);
}

package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM comment WHERE target_id = #{targetId} AND target_type = #{targetType} " +
            "AND parent_id IS NULL AND status = 1 ORDER BY created_at ASC")
    List<Comment> selectRootComments(@Param("targetId") Long targetId, @Param("targetType") String targetType);

    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND status = 1 ORDER BY created_at ASC")
    List<Comment> selectReplies(@Param("parentId") Long parentId);
}

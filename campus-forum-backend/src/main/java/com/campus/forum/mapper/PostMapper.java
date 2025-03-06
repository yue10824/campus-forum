package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT * FROM post WHERE status = 1 ORDER BY view_count DESC, like_count DESC LIMIT #{limit}")
    List<Post> selectHotPosts(@Param("limit") int limit);

    /** 直接 UPDATE，绕过 MyBatis-Plus 逻辑删除拦截 */
    @Update("UPDATE post SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Select("SELECT * FROM activity WHERE status = 1 ORDER BY view_count DESC, like_count DESC LIMIT #{limit}")
    List<Activity> selectHotActivities(@Param("limit") int limit);

    @Select("SELECT a.* FROM activity a " +
            "LEFT JOIN user_behavior ub ON a.id = ub.target_id AND ub.target_type = 'activity' AND ub.user_id = #{userId} " +
            "WHERE ub.user_id IS NULL AND a.status = 1 " +
            "ORDER BY a.view_count DESC LIMIT #{limit}")
    List<Activity> selectExcludingInteracted(@Param("userId") Long userId, @Param("limit") int limit);
}

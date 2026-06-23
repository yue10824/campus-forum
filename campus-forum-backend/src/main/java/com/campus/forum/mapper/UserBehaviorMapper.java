package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.UserBehavior;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND target_type = #{targetType}")
    List<UserBehavior> selectByUserIdAndType(@Param("userId") Long userId, @Param("targetType") String targetType);
}

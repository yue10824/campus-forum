package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.ActivityRegistration;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

    @Select("SELECT COUNT(*) FROM activity_registration WHERE activity_id = #{activityId} AND status IN (0,1)")
    int countActiveRegistrations(@Param("activityId") Long activityId);

    @Select("SELECT * FROM activity_registration WHERE activity_id = #{activityId} AND user_id = #{userId}")
    ActivityRegistration findByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);
}

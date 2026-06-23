package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * FROM message WHERE (sender_id = #{uid1} AND receiver_id = #{uid2}) OR " +
            "(sender_id = #{uid2} AND receiver_id = #{uid1}) ORDER BY created_at ASC")
    List<Message> findChatHistory(@Param("uid1") Long uid1, @Param("uid2") Long uid2);
}

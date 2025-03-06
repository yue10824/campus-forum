package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long targetId;
    /** 目标类型：post/activity */
    private String targetType;
    /** 行为类型：VIEW/LIKE/COLLECT/COMMENT */
    private String behaviorType;
    private Float score;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

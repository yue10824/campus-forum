package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 通知类型：LIKE/COMMENT/REPLY/FOLLOW/SYSTEM/REGISTER */
    private String type;
    private Long actorId;
    private Long targetId;
    private String targetType;
    private String content;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

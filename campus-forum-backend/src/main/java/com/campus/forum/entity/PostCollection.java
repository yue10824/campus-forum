package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_collection")
public class PostCollection {
    private Long userId;
    private Long targetId;
    /** 目标类型：post/activity */
    private String targetType;
    private LocalDateTime createdAt;
}

package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体（支持帖子/活动楼中楼）
 */
@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long targetId;
    /** 目标类型：post / activity */
    private String targetType;
    private Long userId;
    private Long parentId;
    private Long replyUid;
    private String content;
    private Integer likeCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 子回复列表（非数据库字段） */
    @TableField(exist = false)
    private List<Comment> replies;
}

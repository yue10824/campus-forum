package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子实体
 */
@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer sectionId;
    private String title;
    private String content;
    private String coverImage;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer viewCount;
    /**
     * 状态：0草稿 1已发布 2已删除 3审核中
     */
    private Integer status;
    private Integer isTop;
    private Integer isEssence;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

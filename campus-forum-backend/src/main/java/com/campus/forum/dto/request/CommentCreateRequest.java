package com.campus.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @NotBlank(message = "目标类型不能为空")
    private String targetType; // post / activity

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;   // 楼中楼父评论ID
    private Long replyUid;   // 被回复用户ID
}

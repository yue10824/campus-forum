package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat_history")
public class AiChatHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 角色：user/assistant */
    private String role;
    private String content;
    private String sessionId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

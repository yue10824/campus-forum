package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private String email;
    /** 角色：0普通用户 1管理员 */
    private Integer role;
    /** 状态：0禁用 1正常 */
    private Integer status;
    private Integer postCount;
    private Integer followCount;
    private Integer fanCount;
    /** 经验值 */
    private Integer exp;
    /** 等级 1-10 */
    private Integer level;
    /** 禁止发帖截止时间，NULL表示未被禁 */
    private LocalDateTime postBanUntil;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

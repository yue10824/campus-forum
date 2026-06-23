package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("section")
public class Section {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String code;
    private String description;
    private String icon;
    @TableField("sort_order")
    private Integer sort;
    private Integer postCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

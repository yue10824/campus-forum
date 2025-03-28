package com.campus.forum.dto;

import lombok.Data;

@Data
public class PostCreateRequest {
    private Integer sectionId;
    private String title;
    private String content;
    private String coverImage;
}

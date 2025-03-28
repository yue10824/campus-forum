package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.entity.Post;

public interface PostService {
    Post createPost(Long userId, PostCreateRequest req);
    Page<Post> listPosts(int pageNum, int pageSize, Long sectionId, String keyword);
    Post getPostById(Long id, Long currentUserId);
    boolean deletePost(Long postId, Long currentUserId);
    boolean toggleLike(Long postId, Long userId);
}

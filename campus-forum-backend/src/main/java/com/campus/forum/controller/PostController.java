package com.campus.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.entity.Post;
import com.campus.forum.security.JwtTokenProvider;
import com.campus.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "帖子模块")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "帖子列表（分页）")
    @GetMapping
    public Result<Page<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String keyword) {
        return Result.success(postService.listPosts(page, size, sectionId, keyword));
    }

    @Operation(summary = "帖子详情")
    @GetMapping("/{id}")
    public Result<Post> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequestQuietly(request);
        return Result.success(postService.getPostById(id, userId));
    }

    @Operation(summary = "发布帖子")
    @PostMapping
    public Result<Post> create(@Valid @RequestBody PostCreateRequest req, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        return Result.success(postService.createPost(userId, req));
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        postService.deletePost(id, userId);
        return Result.success();
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        boolean liked = postService.toggleLike(id, userId);
        return Result.success(liked);
    }
}

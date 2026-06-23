package com.campus.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.entity.User;
import com.campus.forum.security.JwtTokenProvider;
import com.campus.forum.service.PostService;
import com.campus.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "获取用户信息")
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "编辑个人资料")
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        return Result.success(userService.updateProfile(userId,
                body.get("nickname"), body.get("avatar"), body.get("bio")));
    }

    @Operation(summary = "关注/取关")
    @PostMapping("/{id}/follow")
    public Result<Boolean> toggleFollow(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = jwtTokenProvider.getUserIdFromRequest(request);
        boolean following = userService.toggleFollow(currentUserId, id);
        return Result.success(following);
    }

    @Operation(summary = "粉丝列表")
    @GetMapping("/{id}/followers")
    public Result<Page<User>> followers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getFollowers(id, page, size));
    }

    @Operation(summary = "关注列表")
    @GetMapping("/{id}/following")
    public Result<Page<User>> following(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getFollowing(id, page, size));
    }

    @Operation(summary = "用户帖子列表")
    @GetMapping("/{id}/posts")
    public Result<?> userPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.listPosts(page, size, null, null));
    }

    @Operation(summary = "检查是否关注")
    @GetMapping("/{id}/is-following")
    public Result<Boolean> isFollowing(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = jwtTokenProvider.getUserIdFromRequest(request);
        return Result.success(userService.isFollowing(currentUserId, id));
    }
}

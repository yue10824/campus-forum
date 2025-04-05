package com.campus.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.entity.PostCollection;
import com.campus.forum.security.JwtTokenProvider;
import com.campus.forum.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "收藏模块")
@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "收藏/取消收藏")
    @PostMapping
    public Result<Boolean> toggle(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String targetType = body.get("targetType").toString();
        boolean collected = collectionService.toggleCollection(userId, targetId, targetType);
        return Result.success(collected);
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping("/my")
    public Result<Page<PostCollection>> myCollections(
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        return Result.success(collectionService.myCollections(userId, targetType, page, size));
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> check(
            @RequestParam Long targetId,
            @RequestParam String targetType,
            HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        return Result.success(collectionService.isCollected(userId, targetId, targetType));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{targetId}")
    public Result<Void> cancel(
            @PathVariable Long targetId,
            @RequestParam String targetType,
            HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        collectionService.toggleCollection(userId, targetId, targetType);
        return Result.success();
    }
}

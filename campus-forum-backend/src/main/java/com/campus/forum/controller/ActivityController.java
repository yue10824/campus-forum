package com.campus.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.dto.request.ActivityCreateRequest;
import com.campus.forum.entity.Activity;
import com.campus.forum.service.ActivityService;
import com.campus.forum.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 */
@Tag(name = "活动接口")
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final RecommendService recommendService;

    @Operation(summary = "获取活动列表")
    @GetMapping
    public Result<Page<Activity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(activityService.listActivities(page, size, sectionId, status, keyword));
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return Result.success(activityService.getActivityDetail(id, userId));
    }

    @Operation(summary = "发布活动")
    @PostMapping
    public Result<Activity> create(@Valid @RequestBody ActivityCreateRequest req,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(activityService.createActivity(req, userId));
    }

    @Operation(summary = "点赞/取消点赞活动")
    @PostMapping("/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(activityService.toggleLike(id, userId));
    }

    @Operation(summary = "收藏/取消收藏活动")
    @PostMapping("/{id}/collect")
    public Result<Boolean> toggleCollect(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(activityService.toggleCollect(id, userId));
    }

    @Operation(summary = "协同过滤推荐活动")
    @GetMapping("/recommend")
    public Result<List<Activity>> recommend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(recommendService.recommendActivities(userId, size));
    }
}

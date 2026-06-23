package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.entity.Notification;
import com.campus.forum.mapper.NotificationMapper;
import com.campus.forum.security.JwtTokenProvider;
import com.campus.forum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知模块")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "通知列表（分页）")
    @GetMapping
    public Result<Page<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        Page<Notification> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);
        return Result.success(notificationMapper.selectPage(pageObj, wrapper));
    }

    @Operation(summary = "标记所有为已读")
    @PutMapping("/read-all")
    public Result<Void> readAll(HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        notificationService.markAllRead(userId);
        return Result.success();
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public Result<Void> readOne(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        notificationService.markRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        return Result.success(count);
    }
}

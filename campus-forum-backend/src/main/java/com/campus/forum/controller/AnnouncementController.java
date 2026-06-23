package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.common.Result;
import com.campus.forum.entity.Announcement;
import com.campus.forum.mapper.AnnouncementMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公告-公开")
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;

    @Operation(summary = "获取有效公告列表")
    @GetMapping
    public Result<List<Announcement>> list(
            @RequestParam(defaultValue = "10") int size) {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getIsActive, 1)
                        .orderByDesc(Announcement::getCreatedAt)
                        .last("LIMIT " + size));
        return Result.success(list);
    }
}

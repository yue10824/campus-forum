package com.campus.forum.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.Announcement;
import com.campus.forum.mapper.AnnouncementMapper;
import com.campus.forum.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-公告管理")
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementMapper announcementMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "公告列表")
    @GetMapping
    public Result<Page<Announcement>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Announcement> pageObj = new Page<>(page, size);
        return Result.success(announcementMapper.selectPage(pageObj,
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedAt)));
    }

    @Operation(summary = "发布公告")
    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromRequest(request);
        announcement.setAdminId(userId);
        announcement.setIsActive(1);
        announcementMapper.insert(announcement);
        return Result.success(announcement);
    }

    @Operation(summary = "启用/停用公告")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Announcement ann = announcementMapper.selectById(id);
        if (ann == null) throw new BusinessException("公告不存在");
        ann.setIsActive(status);
        announcementMapper.updateById(ann);
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }
}

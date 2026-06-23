package com.campus.forum.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.Activity;
import com.campus.forum.mapper.ActivityMapper;
import com.campus.forum.mapper.ActivityRegistrationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-活动管理")
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;

    @Operation(summary = "活动列表（分页+筛选）")
    @GetMapping
    public Result<Page<Activity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<Activity> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .like(StringUtils.hasText(keyword), Activity::getTitle, keyword)
                .eq(status != null, Activity::getStatus, status)
                .orderByDesc(Activity::getCreatedAt);
        return Result.success(activityMapper.selectPage(pageObj, wrapper));
    }

    @Operation(summary = "审核活动（上线/下线）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) throw new BusinessException("活动不存在");
        activity.setStatus(status);
        activityMapper.updateById(activity);
        return Result.success();
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) throw new BusinessException("活动不存在");
        activity.setStatus(0);
        activityMapper.updateById(activity);
        return Result.success();
    }

    @Operation(summary = "活动报名人数统计")
    @GetMapping("/{id}/registrations/count")
    public Result<Long> registrationCount(@PathVariable Long id) {
        long count = registrationMapper.selectCount(
                new LambdaQueryWrapper<com.campus.forum.entity.ActivityRegistration>()
                        .eq(com.campus.forum.entity.ActivityRegistration::getActivityId, id)
        );
        return Result.success(count);
    }
}

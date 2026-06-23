package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.Activity;
import com.campus.forum.entity.ActivityRegistration;
import com.campus.forum.mapper.ActivityMapper;
import com.campus.forum.mapper.ActivityRegistrationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动报名控制器
 */
@Tag(name = "活动报名接口")
@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;

    @Operation(summary = "报名活动")
    @PostMapping
    @Transactional
    public Result<ActivityRegistration> register(
            @RequestParam Long activityId,
            @RequestParam(required = false) String remark,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        if (activity.getStatus() != 1) throw new BusinessException("活动当前不在报名中");

        // 检查是否已报名
        if (registrationMapper.findByActivityAndUser(activityId, userId) != null) {
            throw new BusinessException("您已报名过该活动");
        }
        // 检查人数上限
        if (activity.getMaxParticipants() != null) {
            int count = registrationMapper.countActiveRegistrations(activityId);
            if (count >= activity.getMaxParticipants()) {
                throw new BusinessException("报名人数已达上限");
            }
        }

        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(activityId);
        reg.setUserId(userId);
        reg.setStatus(0); // 待审核
        reg.setRemark(remark);
        registrationMapper.insert(reg);

        // 更新活动当前报名人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityMapper.updateById(activity);

        return Result.success(reg);
    }

    @Operation(summary = "取消报名")
    @DeleteMapping("/{activityId}")
    @Transactional
    public Result<Void> cancel(@PathVariable Long activityId,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ActivityRegistration reg = registrationMapper.findByActivityAndUser(activityId, userId);
        if (reg == null) throw new BusinessException("未找到报名记录");

        reg.setStatus(3); // 已取消
        registrationMapper.updateById(reg);

        Activity activity = activityMapper.selectById(activityId);
        if (activity != null) {
            activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
            activityMapper.updateById(activity);
        }
        return Result.success();
    }

    @Operation(summary = "我的报名记录")
    @GetMapping("/my")
    public Result<List<ActivityRegistration>> myRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(registrationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .orderByDesc(ActivityRegistration::getCreatedAt)));
    }

    @Operation(summary = "查看某活动的报名人员列表")
    @GetMapping("/activity/{id}")
    public Result<List<ActivityRegistration>> activityRegistrations(@PathVariable Long id) {
        return Result.success(registrationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, id)
                        .orderByDesc(ActivityRegistration::getCreatedAt)));
    }

    @Operation(summary = "审核报名（通过/拒绝）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        ActivityRegistration reg = registrationMapper.selectById(id);
        if (reg == null) throw new BusinessException("报名记录不存在");
        reg.setStatus(status);
        registrationMapper.updateById(reg);
        return Result.success();
    }
}

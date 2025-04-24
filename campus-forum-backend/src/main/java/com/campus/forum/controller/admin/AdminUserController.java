package com.campus.forum.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 用户管理
 */
@Tag(name = "管理端-用户管理")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Operation(summary = "用户列表（分页+搜索）")
    @GetMapping
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<User> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(keyword), User::getNickname, keyword)
                .or(StringUtils.hasText(keyword))
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .orderByDesc(User::getCreatedAt);
        return Result.success(userMapper.selectPage(pageObj, wrapper));
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "新增用户（管理员手动创建）")
    @PostMapping
    public Result<Void> create(@RequestBody User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "123456"));
        user.setStatus(1);
        user.setPostCount(0);
        userMapper.insert(user);
        return Result.success();
    }

    @Operation(summary = "解除用户禁发帖")
    @PostMapping("/{id}/unban-post")
    public Result<Void> unbanPost(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        userMapper.unbanPost(id);
        notificationService.sendNotification(
                id, "SYSTEM", null, null, null,
                "管理员已解除您的发帖限制，您现在可以正常发帖了。");
        return Result.success();
    }
}

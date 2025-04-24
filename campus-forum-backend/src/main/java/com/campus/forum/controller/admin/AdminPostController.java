package com.campus.forum.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.Result;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.AiService;
import com.campus.forum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理端-帖子管理")
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final AiService aiService;
    private final NotificationService notificationService;

    @Operation(summary = "帖子列表（分页+筛选）")
    @GetMapping
    public Result<Page<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<Post> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .ne(Post::getStatus, -1)  // 默认不显示已删除
                .like(StringUtils.hasText(keyword), Post::getTitle, keyword)
                .eq(status != null, Post::getStatus, status)
                .orderByDesc(Post::getCreatedAt);
        return Result.success(postMapper.selectPage(pageObj, wrapper));
    }

    @Operation(summary = "审核帖子（上线/下线）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        postMapper.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "置顶帖子")
    @PutMapping("/{id}/top")
    public Result<Void> top(@PathVariable Long id, @RequestParam Integer isTop) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        post.setIsTop(isTop);
        postMapper.updateById(post);
        return Result.success();
    }

    @Operation(summary = "加精帖子")
    @PutMapping("/{id}/essence")
    public Result<Void> essence(@PathVariable Long id, @RequestParam Integer isEssence) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        post.setIsEssence(isEssence);
        postMapper.updateById(post);
        return Result.success();
    }

    @Operation(summary = "AI辅助审核（违规检测）")
    @PostMapping("/{id}/ai-review")
    public Result<Map<String, Object>> aiReview(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        Map<String, Object> result = aiService.reviewContent(post.getTitle() + "\n" + post.getContent());
        return Result.success(result);
    }

    @Operation(summary = "AI检测后一键下线+禁发30分钟+通知用户")
    @PostMapping("/{id}/ai-takedown")
    public Result<Void> aiTakedown(@PathVariable Long id,
                                   @RequestParam(required = false, defaultValue = "") String reason) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");

        // 1. 下线帖子
        postMapper.updateStatus(id, 0);

        // 2. 禁止发帖30分钟
        LocalDateTime banUntil = LocalDateTime.now().plusMinutes(30);
        userMapper.banPost(post.getUserId(), banUntil);

        // 3. 发站内通知给发帖人
        String noticeContent = "您的帖子《" + post.getTitle() + "》因包含违规内容已被AI检测系统自动下线，"
                + "并禁止发帖30分钟。"
                + (StringUtils.hasText(reason) ? "违规原因：" + reason : "")
                + "如有疑问请联系管理员。";
        notificationService.sendNotification(
                post.getUserId(), "SYSTEM", null,
                post.getId(), "post", noticeContent);

        return Result.success();
    }

    @Operation(summary = "解除用户禁发帖（通过帖子ID找到发帖人）")
    @PostMapping("/{id}/unban-post")
    public Result<Void> unbanByPost(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        userMapper.unbanPost(post.getUserId());

        // 通知用户已解除禁发
        notificationService.sendNotification(
                post.getUserId(), "SYSTEM", null,
                post.getId(), "post",
                "管理员已解除您的发帖限制，您现在可以正常发帖了。");
        return Result.success();
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        postMapper.updateStatus(id, -1);
        return Result.success();
    }
}

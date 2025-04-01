package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.dto.request.CommentCreateRequest;
import com.campus.forum.entity.Comment;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostLikeMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评论互动控制器
 */
@Tag(name = "评论接口")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentMapper commentMapper;
    private final PostLikeMapper likeMapper;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Operation(summary = "获取评论树（帖子/活动通用）")
    @GetMapping
    public Result<List<Comment>> listComments(
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "post") String targetType) {
        List<Comment> roots = commentMapper.selectRootComments(targetId, targetType);
        // 为每条一级评论加载回复
        roots.forEach(root -> root.setReplies(commentMapper.selectReplies(root.getId())));
        return Result.success(roots);
    }

    @Operation(summary = "发表评论/回复")
    @PostMapping
    @Transactional
    public Result<Comment> createComment(@Valid @RequestBody CommentCreateRequest req,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Comment comment = new Comment();
        comment.setTargetId(req.getTargetId());
        comment.setTargetType(req.getTargetType());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId());
        comment.setReplyUid(req.getReplyUid());
        comment.setContent(req.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        // 回帖奖励5点经验值
        userMapper.addExp(userId, 5);

        // 发送通知
        String notifyType = req.getParentId() != null ? "REPLY" : "COMMENT";
        if (req.getReplyUid() != null && !req.getReplyUid().equals(userId)) {
            notificationService.sendNotification(req.getReplyUid(), notifyType, userId,
                    req.getTargetId(), req.getTargetType(), "有人回复了您的评论");
        }
        return Result.success(comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) return Result.error("评论不存在");
        Long userId = Long.parseLong(userDetails.getUsername());
        if (!comment.getUserId().equals(userId)) {
            return Result.error(403, "无权删除");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
        return Result.success();
    }

    @Operation(summary = "评论点赞")
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> likeComment(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        int liked = likeMapper.checkLiked(userId, id, "comment");
        Comment comment = commentMapper.selectById(id);
        if (comment == null) return Result.error("评论不存在");

        if (liked > 0) {
            likeMapper.deleteLike(userId, id, "comment");
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(comment);
            return Result.success(Map.of("liked", false, "likeCount", comment.getLikeCount()));
        } else {
            com.campus.forum.entity.PostLike like = new com.campus.forum.entity.PostLike();
            like.setUserId(userId);
            like.setTargetId(id);
            like.setTargetType("comment");
            like.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
            return Result.success(Map.of("liked", true, "likeCount", comment.getLikeCount()));
        }
    }
}

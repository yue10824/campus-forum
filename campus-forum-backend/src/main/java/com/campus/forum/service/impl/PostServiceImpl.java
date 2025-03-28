package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostLike;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserBehavior;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.PostLikeMapper;
import com.campus.forum.mapper.UserBehaviorMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.AiService;
import com.campus.forum.service.NotificationService;
import com.campus.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final UserMapper userMapper;
    private final AiService aiService;
    private final NotificationService notificationService;

    @Override
    public Post createPost(Long userId, PostCreateRequest req) {
        // 检查用户是否被禁止发帖
        User user = userMapper.findById(userId);
        if (user != null && user.getPostBanUntil() != null
                && LocalDateTime.now().isBefore(user.getPostBanUntil())) {
            long minutes = java.time.Duration.between(LocalDateTime.now(), user.getPostBanUntil()).toMinutes() + 1;
            throw new BusinessException("您因发帖违规内容被禁止发帖，还有 " + minutes + " 分钟解除");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setSectionId(req.getSectionId());
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCoverImage(req.getCoverImage());
        post.setStatus(1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);
        // 发帖奖励10点经验值
        userMapper.addExp(userId, 10);

        // 发帖后异步AI审核（违规自动下线+禁发30分钟+通知）
        final Long postId = post.getId();
        final String postTitle = post.getTitle();
        final Long authorId = userId;
        final String reviewContent = req.getTitle() + "\n" + req.getContent();
        new Thread(() -> {
            try {
                Map<String, Object> review = aiService.reviewContent(reviewContent);
                int score = 0;
                Object scoreObj = review.get("violationScore");
                if (scoreObj instanceof Number) score = ((Number) scoreObj).intValue();
                if (score >= 60) {
                    // 违规：下线帖子
                    postMapper.updateStatus(postId, 0);
                    // 禁止发帖30分钟
                    LocalDateTime banUntil = LocalDateTime.now().plusMinutes(30);
                    userMapper.banPost(authorId, banUntil);
                    // 站内通知
                    String violationType = review.get("violationType") != null ? String.valueOf(review.get("violationType")) : "违规内容";
                    String suggestion = review.get("suggestion") != null ? String.valueOf(review.get("suggestion")) : "";
                    String notice = "您的帖子《" + postTitle + "》经AI系统检测，存在违规内容（" + violationType + "），已被自动下线，" +
                            "并禁止发帖30分钟。" + (StringUtils.hasText(suggestion) ? "建议：" + suggestion : "") +
                            "如有异议请联系管理员。";
                    notificationService.sendNotification(authorId, "SYSTEM", null, postId, "post", notice);
                    log.info("[AI审核] 帖子 {} 违规下线，score={}, type={}", postId, score, violationType);
                }
            } catch (Exception e) {
                log.warn("[AI审核] 帖子 {} 审核失败: {}", postId, e.getMessage());
            }
        }).start();

        return post;
    }

    @Override
    public Page<Post> listPosts(int pageNum, int pageSize, Long sectionId, String keyword) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .eq(sectionId != null, Post::getSectionId, sectionId)
                .like(StringUtils.hasText(keyword), Post::getTitle, keyword)
                .orderByDesc(Post::getCreatedAt);
        return postMapper.selectPage(page, wrapper);
    }

    @Override
    public Post getPostById(Long id, Long currentUserId) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
        // 记录行为
        if (currentUserId != null) {
            recordBehavior(currentUserId, id, "post", "VIEW");
        }
        return post;
    }

    @Override
    public boolean deletePost(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权删除他人帖子");
        }
        post.setStatus(0);
        return postMapper.updateById(post) > 0;
    }

    @Override
    public boolean toggleLike(Long postId, Long userId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getTargetId, postId)
                .eq(PostLike::getTargetType, "post")
                .eq(PostLike::getUserId, userId);
        PostLike existing = postLikeMapper.selectOne(wrapper);
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        if (existing != null) {
            postLikeMapper.delete(wrapper);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
            return false;
        } else {
            PostLike like = new PostLike();
            like.setTargetId(postId);
            like.setTargetType("post");
            like.setUserId(userId);
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
            // 被点赞者加2点经验值
            userMapper.addExp(post.getUserId(), 2);
            recordBehavior(userId, postId, "post", "LIKE");
            return true;
        }
    }

    private void recordBehavior(Long userId, Long targetId, String targetType, String behaviorType) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setTargetId(targetId);
        behavior.setTargetType(targetType);
        behavior.setBehaviorType(behaviorType);
        userBehaviorMapper.insert(behavior);
    }
}

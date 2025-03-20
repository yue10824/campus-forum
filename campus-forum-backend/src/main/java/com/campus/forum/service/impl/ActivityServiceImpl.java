package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.dto.request.ActivityCreateRequest;
import com.campus.forum.entity.*;
import com.campus.forum.mapper.*;
import com.campus.forum.service.ActivityService;
import com.campus.forum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final PostLikeMapper likeMapper;
    private final PostCollectionMapper collectionMapper;
    private final UserBehaviorMapper behaviorMapper;
    private final NotificationService notificationService;

    @Override
    public Page<Activity> listActivities(int page, int size, Integer sectionId, Integer status, String keyword) {
        Page<Activity> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, status != null ? status : 1)
                .eq(sectionId != null, Activity::getSectionId, sectionId)
                .like(keyword != null && !keyword.isEmpty(), Activity::getTitle, keyword)
                .orderByDesc(Activity::getIsTop)
                .orderByDesc(Activity::getCreatedAt);
        return activityMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public Activity getActivityDetail(Long id, Long currentUserId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getStatus() == 3) {
            throw new BusinessException("活动不存在");
        }
        // 增加浏览量
        activity.setViewCount(activity.getViewCount() + 1);
        activityMapper.updateById(activity);
        // 记录浏览行为（用于推荐引擎）
        if (currentUserId != null) {
            recordBehavior(currentUserId, id, "activity", "VIEW", 1.0f);
        }
        return activity;
    }

    @Override
    @Transactional
    public Activity createActivity(ActivityCreateRequest req, Long userId) {
        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setSectionId(req.getSectionId());
        activity.setTitle(req.getTitle());
        activity.setDescription(req.getDescription());
        activity.setCoverImage(req.getCoverImage());
        activity.setLocation(req.getLocation());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setMaxParticipants(req.getMaxParticipants());
        activity.setCurrentParticipants(0);
        activity.setLikeCount(0);
        activity.setCommentCount(0);
        activity.setCollectCount(0);
        activity.setViewCount(0);
        activity.setStatus(1); // 报名中
        activity.setIsTop(0);
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");

        int liked = likeMapper.checkLiked(userId, activityId, "activity");
        if (liked > 0) {
            // 取消点赞
            likeMapper.deleteLike(userId, activityId, "activity");
            activity.setLikeCount(Math.max(0, activity.getLikeCount() - 1));
            activityMapper.updateById(activity);
            return false;
        } else {
            // 点赞
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setTargetId(activityId);
            like.setTargetType("activity");
            like.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(like);
            activity.setLikeCount(activity.getLikeCount() + 1);
            activityMapper.updateById(activity);
            // 记录行为
            recordBehavior(userId, activityId, "activity", "LIKE", 3.0f);
            // 发送通知给活动发布者
            notificationService.sendNotification(activity.getUserId(), "LIKE", userId, activityId, "activity",
                    "有人点赞了您的活动：" + activity.getTitle());
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");

        int collected = collectionMapper.checkCollected(userId, activityId, "activity");
        if (collected > 0) {
            collectionMapper.deleteCollection(userId, activityId, "activity");
            activity.setCollectCount(Math.max(0, activity.getCollectCount() - 1));
            activityMapper.updateById(activity);
            return false;
        } else {
            PostCollection col = new PostCollection();
            col.setUserId(userId);
            col.setTargetId(activityId);
            col.setTargetType("activity");
            col.setCreatedAt(LocalDateTime.now());
            collectionMapper.insert(col);
            activity.setCollectCount(activity.getCollectCount() + 1);
            activityMapper.updateById(activity);
            recordBehavior(userId, activityId, "activity", "COLLECT", 4.0f);
            return true;
        }
    }

    private void recordBehavior(Long userId, Long targetId, String targetType, String behaviorType, float score) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setTargetId(targetId);
        behavior.setTargetType(targetType);
        behavior.setBehaviorType(behaviorType);
        behavior.setScore(score);
        behaviorMapper.insert(behavior);
    }
}

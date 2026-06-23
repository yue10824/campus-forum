package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.request.ActivityCreateRequest;
import com.campus.forum.entity.Activity;

public interface ActivityService {
    Page<Activity> listActivities(int page, int size, Integer sectionId, Integer status, String keyword);
    Activity getActivityDetail(Long id, Long currentUserId);
    Activity createActivity(ActivityCreateRequest req, Long userId);
    boolean toggleLike(Long activityId, Long userId);
    boolean toggleCollect(Long activityId, Long userId);
}

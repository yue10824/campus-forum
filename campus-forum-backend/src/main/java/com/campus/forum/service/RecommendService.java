package com.campus.forum.service;

import com.campus.forum.entity.Activity;
import java.util.List;

public interface RecommendService {
    List<Activity> recommendActivities(Long userId, int topN);
}

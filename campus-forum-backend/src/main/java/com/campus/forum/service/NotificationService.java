package com.campus.forum.service;

import com.campus.forum.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Long toUserId, String type, Long actorId,
                          Long targetId, String targetType, String content);
    List<Notification> listNotifications(Long userId, int page, int size);
    int countUnread(Long userId);
    void markAllRead(Long userId);
    void markRead(Long notificationId, Long userId);
}

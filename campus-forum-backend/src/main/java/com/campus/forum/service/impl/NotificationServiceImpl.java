package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Notification;
import com.campus.forum.mapper.NotificationMapper;
import com.campus.forum.service.NotificationService;
import com.campus.forum.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler wsHandler;

    @Override
    public void sendNotification(Long toUserId, String type, Long actorId,
                                 Long targetId, String targetType, String content) {
        Notification notification = new Notification();
        notification.setUserId(toUserId);
        notification.setType(type);
        notification.setActorId(actorId);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setContent(content);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        // 通过 WebSocket 推送实时通知
        wsHandler.sendToUser(toUserId, content);
    }

    @Override
    public List<Notification> listNotifications(Long userId, int page, int size) {
        Page<Notification> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);
        return notificationMapper.selectPage(pageObj, wrapper).getRecords();
    }

    @Override
    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }
}

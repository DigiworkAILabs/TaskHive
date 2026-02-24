package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a notification to a specific user's personal topic.
     */
    public void sendToUser(UUID userId, NotificationResponse notification) {
        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, notification);
        log.debug("WebSocket notification sent to user [{}]: {}", userId, notification.getTitle());
    }

    /**
     * Sends a real-time update to a task-specific topic.
     */
    public void sendToTask(UUID taskId, Object payload) {
        String destination = "/topic/tasks/" + taskId;
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("WebSocket update sent to task [{}]", taskId);
    }

    /**
     * Sends a system-wide announcement to all connected clients.
     */
    public void sendSystemAnnouncement(Object payload) {
        messagingTemplate.convertAndSend("/topic/system", payload);
        log.debug("System-wide WebSocket announcement sent");
    }
}

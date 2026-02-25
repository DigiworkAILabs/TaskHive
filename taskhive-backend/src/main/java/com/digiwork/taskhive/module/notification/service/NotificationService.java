package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.notification.dto.NotificationResponse;
import com.digiwork.taskhive.module.notification.enums.NotificationType;
import com.digiwork.taskhive.module.notification.model.Notification;
import com.digiwork.taskhive.module.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    public PageResponse<NotificationResponse> getNotifications(int page, int size) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return toPageResponse(notifications);
    }

    public PageResponse<NotificationResponse> getUnreadNotifications(int page, int size) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return toPageResponse(notifications);
    }

    public long getUnreadCount() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.digiwork.taskhive.common.exception.ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

        UUID userId = SecurityUtils.getCurrentUserId();
        if (!notification.getUserId().equals(userId)) {
            throw new com.digiwork.taskhive.common.exception.UnauthorizedException(
                    "You can only mark your own notifications as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        UUID userId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public Notification createNotification(UUID userId, NotificationType type, String title,
            String message, String entityType, UUID entityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Created notification [{}] for user [{}]: {}", type, userId, title);

        // Send real-time WebSocket notification
        NotificationResponse response = toResponse(notification);
        webSocketNotificationService.sendToUser(userId, response);

        return notification;
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private PageResponse<NotificationResponse> toPageResponse(Page<Notification> page) {
        return PageResponse.<NotificationResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

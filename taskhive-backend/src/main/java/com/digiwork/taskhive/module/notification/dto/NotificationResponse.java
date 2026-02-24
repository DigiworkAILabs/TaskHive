package com.digiwork.taskhive.module.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime readAt;
    private String entityType;
    private UUID entityId;
    private LocalDateTime createdAt;
}

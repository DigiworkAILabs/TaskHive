package com.digiwork.taskhive.module.notification.dto;

import com.digiwork.taskhive.common.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {

    private PageResponse<NotificationResponse> notifications;
    private long unreadCount;
}

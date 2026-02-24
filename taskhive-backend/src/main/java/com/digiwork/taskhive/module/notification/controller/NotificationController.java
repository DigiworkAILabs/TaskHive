package com.digiwork.taskhive.module.notification.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.notification.dto.NotificationListResponse;
import com.digiwork.taskhive.module.notification.dto.NotificationPreferenceRequest;
import com.digiwork.taskhive.module.notification.dto.NotificationResponse;
import com.digiwork.taskhive.module.notification.model.NotificationPreference;
import com.digiwork.taskhive.module.notification.service.NotificationPreferenceService;
import com.digiwork.taskhive.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;

    // ─── Get Notifications (paginated) ───────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationResponse> notifications = notificationService.getNotifications(page, size);
        long unreadCount = notificationService.getUnreadCount();
        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .build();
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", response));
    }

    // ─── Get Unread Notifications ────────────────────────────────────────────

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationResponse> notifications = notificationService.getUnreadNotifications(page, size);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", notifications));
    }

    // ─── Get Unread Count (badge) ────────────────────────────────────────────

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    // ─── Mark Single Notification as Read ────────────────────────────────────

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    // ─── Mark All Notifications as Read ──────────────────────────────────────

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    // ─── Get Notification Preferences ────────────────────────────────────────

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreference>> getPreferences() {
        NotificationPreference preferences = preferenceService.getPreferences();
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved successfully", preferences));
    }

    // ─── Update Notification Preferences ─────────────────────────────────────

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreference>> updatePreferences(
            @RequestBody NotificationPreferenceRequest request) {
        NotificationPreference preferences = preferenceService.updatePreferences(request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated successfully", preferences));
    }
}

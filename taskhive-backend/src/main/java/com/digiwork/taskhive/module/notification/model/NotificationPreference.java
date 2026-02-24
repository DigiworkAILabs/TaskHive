package com.digiwork.taskhive.module.notification.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "task_assigned", nullable = false)
    @Builder.Default
    private Boolean taskAssigned = true;

    @Column(name = "task_overdue", nullable = false)
    @Builder.Default
    private Boolean taskOverdue = true;

    @Column(name = "daily_digest", nullable = false)
    @Builder.Default
    private Boolean dailyDigest = false;

    @Column(name = "digest_time")
    @Builder.Default
    private LocalTime digestTime = LocalTime.of(8, 0);

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

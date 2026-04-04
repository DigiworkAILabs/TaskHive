package com.digiwork.taskhive.module.notification.scheduler;

import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.notification.model.Notification;
import com.digiwork.taskhive.module.notification.model.NotificationPreference;
import com.digiwork.taskhive.module.notification.enums.NotificationType;
import com.digiwork.taskhive.module.notification.repository.NotificationPreferenceRepository;
import com.digiwork.taskhive.module.notification.repository.NotificationRepository;
import com.digiwork.taskhive.module.notification.service.EmailQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyDigestSchedulerTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailQueueService emailQueueService;

    @InjectMocks
    private DailyDigestScheduler dailyDigestScheduler;

    private UUID userId;
    private NotificationPreference preference;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setDailyDigest(true);
    }

    @Test
    @DisplayName("should skip when no users opted in")
    void shouldSkipWhenNoUsers() {
        when(preferenceRepository.findByDailyDigestTrue()).thenReturn(Collections.emptyList());

        dailyDigestScheduler.sendDailyDigest();

        verifyNoInteractions(notificationRepository, userRepository, emailQueueService);
    }

    @Test
    @DisplayName("should process digest and queue email")
    void shouldQueueEmailForUser() {
        when(preferenceRepository.findByDailyDigestTrue()).thenReturn(List.of(preference));
        
        Notification notification = Notification.builder()
                .userId(userId)
                .title("Test Task")
                .type(NotificationType.TASK_ASSIGNED)
                .createdAt(LocalDateTime.now())
                .build();
        when(notificationRepository.findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), any()))
                .thenReturn(List.of(notification));
                
        User user = User.builder().id(userId).email("test@example.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        dailyDigestScheduler.sendDailyDigest();

        verify(emailQueueService).queueEmail(eq("test@example.com"), anyString(), eq("daily-digest"), anyMap());
    }

    @Test
    @DisplayName("should skip when no unread notifications")
    void shouldSkipWhenNoNotifications() {
        when(preferenceRepository.findByDailyDigestTrue()).thenReturn(List.of(preference));
        when(notificationRepository.findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), any()))
                .thenReturn(Collections.emptyList());

        dailyDigestScheduler.sendDailyDigest();

        verifyNoInteractions(userRepository, emailQueueService);
    }

    @Test
    @DisplayName("should handle user not found gracefully")
    void shouldHandleUserNotFound() {
        when(preferenceRepository.findByDailyDigestTrue()).thenReturn(List.of(preference));
        when(notificationRepository.findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(eq(userId), any()))
                .thenReturn(List.of(new Notification()));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        dailyDigestScheduler.sendDailyDigest();

        verifyNoInteractions(emailQueueService);
    }
}

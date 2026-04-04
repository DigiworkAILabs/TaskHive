package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.notification.dto.NotificationResponse;
import com.digiwork.taskhive.module.notification.enums.NotificationType;
import com.digiwork.taskhive.module.notification.model.Notification;
import com.digiwork.taskhive.module.notification.repository.NotificationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    private UUID currentUserId;
    private UUID notificationId;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testNotification = Notification.builder()
                .id(notificationId)
                .userId(currentUserId)
                .type(NotificationType.TASK_ASSIGNED)
                .title("New Task Assigned")
                .message("You have a new task.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        setSecurityContext(currentUserId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(UUID userId) {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId, "user@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getNotifications Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getNotifications")
    class GetNotificationsTests {

        @Test
        @DisplayName("should return paginated notifications for current user")
        void shouldGetNotifications() {
            Page<Notification> page = new PageImpl<>(List.of(testNotification));

            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(currentUserId), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<NotificationResponse> result = notificationService.getNotifications(0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getUnreadCount Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getUnreadCount")
    class UnreadCountTests {

        @Test
        @DisplayName("should return correct unread count")
        void shouldGetUnreadCount() {
            when(notificationRepository.countByUserIdAndIsReadFalse(currentUserId)).thenReturn(5L);

            long count = notificationService.getUnreadCount();

            assertThat(count).isEqualTo(5);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // markAsRead Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark a notification as read")
        void shouldMarkAsRead() {
            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.of(testNotification));

            notificationService.markAsRead(notificationId);

            assertThat(testNotification.getIsRead()).isTrue();
            verify(notificationRepository).save(testNotification);
        }

        @Test
        @DisplayName("should throw exception when marking another user's notification as read")
        void shouldThrowException_whenMarkingOtherUsersNotification() {
            UUID otherUserId = UUID.randomUUID();
            Notification otherNotification = Notification.builder()
                    .id(notificationId)
                    .userId(otherUserId) // belongs to ANOTHER user
                    .isRead(false)
                    .build();

            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.of(otherNotification));

            assertThatThrownBy(() -> notificationService.markAsRead(notificationId))
                    .isInstanceOf(com.digiwork.taskhive.common.exception.UnauthorizedException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // markAllAsRead Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("should mark all notifications as read for current user")
        void shouldMarkAllAsRead() {
            notificationService.markAllAsRead();

            verify(notificationRepository).markAllAsReadByUserId(currentUserId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createNotification Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createNotification")
    class CreateNotificationTests {

        @Test
        @DisplayName("should create notification and send via WebSocket")
        void shouldCreateNotificationAndSendViaWebSocket() {
            Notification saved = Notification.builder()
                    .id(UUID.randomUUID()).userId(currentUserId)
                    .type(NotificationType.TASK_ASSIGNED).title("New Task").message("Message")
                    .isRead(false).build();

            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            notificationService.createNotification(
                    currentUserId, NotificationType.TASK_ASSIGNED, "New Task", "Message", "TASK", null);

            verify(notificationRepository).save(any(Notification.class));
            verify(webSocketNotificationService).sendToUser(eq(currentUserId), any(NotificationResponse.class));
        }
    }
}

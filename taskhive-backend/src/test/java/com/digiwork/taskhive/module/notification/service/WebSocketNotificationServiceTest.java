package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.notification.dto.NotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    @Test
    @DisplayName("should send notification to user topic")
    void shouldSendToUserTopic() {
        UUID userId = UUID.randomUUID();
        NotificationResponse payload = NotificationResponse.builder()
                .title("Test").message("Test notification").build();

        webSocketNotificationService.sendToUser(userId, payload);

        verify(messagingTemplate).convertAndSend("/topic/notifications/" + userId, payload);
    }

    @Test
    @DisplayName("should send notification to task topic")
    void shouldSendToTaskTopic() {
        UUID taskId = UUID.randomUUID();
        Object payload = "Task update";

        webSocketNotificationService.sendToTask(taskId, payload);

        verify(messagingTemplate).convertAndSend("/topic/tasks/" + taskId, payload);
    }

    @Test
    @DisplayName("should send system-wide announcement")
    void shouldSendSystemAnnouncement() {
        Object payload = "System announcement";

        webSocketNotificationService.sendSystemAnnouncement(payload);

        verify(messagingTemplate).convertAndSend("/topic/system", payload);
    }
}

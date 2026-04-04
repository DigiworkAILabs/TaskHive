package com.digiwork.taskhive.module.notification.scheduler;

import com.digiwork.taskhive.module.notification.enums.EmailStatus;
import com.digiwork.taskhive.module.notification.model.EmailQueue;
import com.digiwork.taskhive.module.notification.repository.EmailQueueRepository;
import com.digiwork.taskhive.module.notification.service.NotificationEmailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailQueueSchedulerTest {

    @Mock
    private EmailQueueRepository emailQueueRepository;
    @Mock
    private NotificationEmailService emailService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailQueueScheduler emailQueueScheduler;

    private EmailQueue pendingEmail;

    @BeforeEach
    void setUp() {
        pendingEmail = EmailQueue.builder()
                .id(UUID.randomUUID())
                .toEmail("test@example.com")
                .subject("Test")
                .templateName("test-template")
                .templateData("{\"key\":\"value\"}")
                .status(EmailStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .scheduledAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    @Test
    @DisplayName("processEmailQueue: should process pending emails successfully")
    @SuppressWarnings("unchecked")
    void processEmailQueue_Success() throws Exception {
        when(emailQueueRepository.findByStatusAndAttemptsLessThanAndScheduledAtBeforeOrderByScheduledAtAsc(
                eq(EmailStatus.PENDING), eq(3), any(LocalDateTime.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(pendingEmail));

        Map<String, Object> data = Map.of("key", "value");
        when(objectMapper.readValue(eq("{\"key\":\"value\"}"), any(TypeReference.class)))
                .thenReturn(data);

        emailQueueScheduler.processEmailQueue();

        verify(emailService).sendEmailFromTemplate(eq("test@example.com"), eq("Test"), eq("test-template"), anyMap());
        verify(emailQueueRepository).save(argThat(email -> email.getStatus() == EmailStatus.SENT));
    }

    @Test
    @DisplayName("processEmailQueue: should handle failures and increment attempts")
    void processEmailQueue_Failure() throws Exception {
        when(emailQueueRepository.findByStatusAndAttemptsLessThanAndScheduledAtBeforeOrderByScheduledAtAsc(
                eq(EmailStatus.PENDING), eq(3), any(LocalDateTime.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(pendingEmail));

        doThrow(new RuntimeException("Mail server down"))
                .when(emailService).sendEmailFromTemplate(anyString(), anyString(), anyString(), anyMap());

        emailQueueScheduler.processEmailQueue();

        verify(emailQueueRepository).save(argThat(email -> 
            email.getAttempts() == 1 && 
            email.getStatus() == EmailStatus.PENDING &&
            email.getScheduledAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    @DisplayName("processEmailQueue: should set status to FAILED on max attempts reached")
    void processEmailQueue_MaxAttempts() throws Exception {
        pendingEmail.setAttempts(2);
        when(emailQueueRepository.findByStatusAndAttemptsLessThanAndScheduledAtBeforeOrderByScheduledAtAsc(
                eq(EmailStatus.PENDING), eq(3), any(LocalDateTime.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(pendingEmail));

        doThrow(new RuntimeException("Final fail"))
                .when(emailService).sendEmailFromTemplate(anyString(), anyString(), anyString(), anyMap());

        emailQueueScheduler.processEmailQueue();

        verify(emailQueueRepository).save(argThat(email -> email.getStatus() == EmailStatus.FAILED));
    }
}

package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.notification.enums.EmailStatus;
import com.digiwork.taskhive.module.notification.repository.EmailQueueRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailQueueServiceTest {

    @Mock
    private EmailQueueRepository emailQueueRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailQueueService emailQueueService;

    @Test
    @DisplayName("queueEmail: should serialize data and save to repository")
    void queueEmail_Success() throws JsonProcessingException {
        String to = "to@example.com";
        String subject = "Subject";
        String template = "template";
        Map<String, Object> data = Map.of("key", "value");
        String json = "{\"key\":\"value\"}";

        when(objectMapper.writeValueAsString(data)).thenReturn(json);

        emailQueueService.queueEmail(to, subject, template, data);

        verify(emailQueueRepository).save(argThat(email -> 
            email.getToEmail().equals(to) &&
            email.getSubject().equals(subject) &&
            email.getTemplateName().equals(template) &&
            email.getTemplateData().equals(json) &&
            email.getStatus() == EmailStatus.PENDING &&
            email.getScheduledAt() != null
        ));
    }

    @Test
    @DisplayName("queueEmail: should handle serialization errors gracefully")
    void queueEmail_SerializationError() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Error") {});

        emailQueueService.queueEmail("to@ex.com", "Sub", "tmp", Map.of());

        verifyNoInteractions(emailQueueRepository);
    }
}

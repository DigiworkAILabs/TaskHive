package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.notification.model.EmailQueue;
import com.digiwork.taskhive.module.notification.repository.EmailQueueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailQueueServiceTest {

    @Mock
    private EmailQueueRepository emailQueueRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailQueueService emailQueueService;

    @Test
    @DisplayName("should queue an email successfully with template data")
    void shouldQueueEmailSuccessfully() throws Exception {
        Map<String, Object> templateData = Map.of("name", "John", "link", "http://example.com");
        when(objectMapper.writeValueAsString(templateData))
                .thenReturn("{\"name\":\"John\",\"link\":\"http://example.com\"}");

        emailQueueService.queueEmail("user@example.com", "Welcome", "activation", templateData);

        ArgumentCaptor<EmailQueue> captor = ArgumentCaptor.forClass(EmailQueue.class);
        verify(emailQueueRepository).save(captor.capture());
        EmailQueue saved = captor.getValue();
        assertThat(saved.getToEmail()).isEqualTo("user@example.com");
        assertThat(saved.getSubject()).isEqualTo("Welcome");
        assertThat(saved.getTemplateName()).isEqualTo("activation");
        assertThat(saved.getTemplateData()).contains("John");
    }

    @Test
    @DisplayName("should queue email with empty template data")
    void shouldQueueEmailWithEmptyData() throws Exception {
        Map<String, Object> templateData = Map.of();
        when(objectMapper.writeValueAsString(templateData)).thenReturn("{}");

        emailQueueService.queueEmail("admin@example.com", "Report", "report-template", templateData);

        ArgumentCaptor<EmailQueue> captor = ArgumentCaptor.forClass(EmailQueue.class);
        verify(emailQueueRepository).save(captor.capture());
        EmailQueue saved = captor.getValue();
        assertThat(saved.getToEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getTemplateName()).isEqualTo("report-template");
    }
}

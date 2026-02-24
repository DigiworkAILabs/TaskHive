package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.notification.enums.EmailStatus;
import com.digiwork.taskhive.module.notification.model.EmailQueue;
import com.digiwork.taskhive.module.notification.repository.EmailQueueRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final EmailQueueRepository emailQueueRepository;
    private final ObjectMapper objectMapper;

    /**
     * Queues an email for asynchronous sending by the EmailQueueScheduler.
     * The email will be processed within 30 seconds by the scheduler.
     */
    @Transactional
    public void queueEmail(String toEmail, String subject, String templateName,
            Map<String, Object> templateData) {
        try {
            String templateDataJson = objectMapper.writeValueAsString(templateData);

            EmailQueue emailQueue = EmailQueue.builder()
                    .toEmail(toEmail)
                    .subject(subject)
                    .templateName(templateName)
                    .templateData(templateDataJson)
                    .status(EmailStatus.PENDING)
                    .scheduledAt(LocalDateTime.now())
                    .build();

            emailQueueRepository.save(emailQueue);
            log.info("Email queued for [{}] with template [{}]", toEmail, templateName);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize template data for email to [{}]: {}", toEmail, e.getMessage());
        }
    }
}

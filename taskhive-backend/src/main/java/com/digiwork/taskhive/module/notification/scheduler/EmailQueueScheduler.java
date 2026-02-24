package com.digiwork.taskhive.module.notification.scheduler;

import com.digiwork.taskhive.module.notification.enums.EmailStatus;
import com.digiwork.taskhive.module.notification.model.EmailQueue;
import com.digiwork.taskhive.module.notification.repository.EmailQueueRepository;
import com.digiwork.taskhive.module.notification.service.NotificationEmailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueScheduler {

    private final EmailQueueRepository emailQueueRepository;
    private final NotificationEmailService emailService;
    private final ObjectMapper objectMapper;

    /**
     * Processes pending emails every 30 seconds.
     * Fetches up to 10 pending emails per cycle.
     * Retries failed emails up to max_attempts (default 3).
     * Exponential backoff is achieved by the scheduler naturally re-picking
     * eligible emails on subsequent runs.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processEmailQueue() {
        List<EmailQueue> pendingEmails = emailQueueRepository
                .findByStatusAndAttemptsLessThanAndScheduledAtBeforeOrderByScheduledAtAsc(
                        EmailStatus.PENDING, 3, LocalDateTime.now(), PageRequest.of(0, 10));

        if (pendingEmails.isEmpty()) {
            return;
        }

        log.info("Processing {} pending emails from queue", pendingEmails.size());

        for (EmailQueue email : pendingEmails) {
            try {
                Map<String, Object> templateData = Map.of();
                if (email.getTemplateData() != null && !email.getTemplateData().isBlank()) {
                    templateData = objectMapper.readValue(
                            email.getTemplateData(), new TypeReference<Map<String, Object>>() {
                            });
                }

                emailService.sendEmailFromTemplate(
                        email.getToEmail(),
                        email.getSubject(),
                        email.getTemplateName(),
                        templateData);

                email.setStatus(EmailStatus.SENT);
                email.setSentAt(LocalDateTime.now());
                emailQueueRepository.save(email);

                log.info("Email sent successfully to [{}] (id: {})", email.getToEmail(), email.getId());

            } catch (Exception e) {
                email.setAttempts(email.getAttempts() + 1);
                email.setErrorMessage(e.getMessage());

                if (email.getAttempts() >= email.getMaxAttempts()) {
                    email.setStatus(EmailStatus.FAILED);
                    log.error("Email to [{}] FAILED permanently after {} attempts: {}",
                            email.getToEmail(), email.getAttempts(), e.getMessage());
                } else {
                    // Schedule retry with exponential backoff
                    long delayMinutes = (long) Math.pow(2, email.getAttempts());
                    email.setScheduledAt(LocalDateTime.now().plusMinutes(delayMinutes));
                    log.warn("Email to [{}] failed (attempt {}/{}), retry in {} minutes: {}",
                            email.getToEmail(), email.getAttempts(), email.getMaxAttempts(),
                            delayMinutes, e.getMessage());
                }

                emailQueueRepository.save(email);
            }
        }
    }
}

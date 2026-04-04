package com.digiwork.taskhive.module.notification.scheduler;

import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.notification.model.Notification;
import com.digiwork.taskhive.module.notification.model.NotificationPreference;
import com.digiwork.taskhive.module.notification.repository.NotificationPreferenceRepository;
import com.digiwork.taskhive.module.notification.repository.NotificationRepository;
import com.digiwork.taskhive.module.notification.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyDigestScheduler {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailQueueService emailQueueService;

    /**
     * Runs daily at 8:00 AM to send digest emails to users who have opted in.
     * Aggregates unread notifications from the past 24 hours.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyDigest() {
        log.info("Starting daily digest email processing...");

        List<NotificationPreference> digestUsers = preferenceRepository.findByDailyDigestTrue();

        if (digestUsers.isEmpty()) {
            log.info("No users opted in for daily digest");
            return;
        }

        LocalDateTime since = LocalDateTime.now().minusHours(24);

        for (NotificationPreference pref : digestUsers) {
            processSingleUserDigest(pref, since);
        }

        log.info("Daily digest processing complete for {} users", digestUsers.size());
    }

    /**
     * Processes the daily digest for a single user preference.
     * Replaces multiple 'continue' statements with early returns to satisfy S135.
     */
    private void processSingleUserDigest(NotificationPreference pref, LocalDateTime since) {
        try {
            List<Notification> unreadNotifications = notificationRepository
                    .findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(
                            pref.getUserId(), since);

            if (unreadNotifications.isEmpty()) {
                log.debug("No unread notifications for user [{}], skipping digest", pref.getUserId());
                return;
            }

            // Resolve user email
            Optional<User> userOpt = userRepository.findById(pref.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("User [{}] not found, skipping digest", pref.getUserId());
                return;
            }

            String email = userOpt.get().getEmail();

            // Build notification summaries for the template
            List<Map<String, String>> notificationSummaries = unreadNotifications.stream()
                    .map(n -> Map.of(
                            "title", n.getTitle(),
                            "message", n.getMessage() != null ? n.getMessage() : "",
                            "type", n.getType().name(),
                            "time", n.getCreatedAt().toString()))
                    .toList();

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("email", email);
            templateData.put("unreadCount", unreadNotifications.size());
            templateData.put("notifications", notificationSummaries);
            templateData.put("date", LocalDateTime.now().toLocalDate().toString());

            emailQueueService.queueEmail(
                    email,
                    "Your Daily Digest - TaskHive",
                    "daily-digest",
                    templateData);

            log.info("Daily digest queued for user [{}] with {} notifications",
                    pref.getUserId(), unreadNotifications.size());

        } catch (Exception e) {
            log.error("Error processing daily digest for user [{}]: {}",
                    pref.getUserId(), e.getMessage());
        }
    }
}

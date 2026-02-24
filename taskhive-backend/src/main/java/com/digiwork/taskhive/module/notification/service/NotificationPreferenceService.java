package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.notification.dto.NotificationPreferenceRequest;
import com.digiwork.taskhive.module.notification.model.NotificationPreference;
import com.digiwork.taskhive.module.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * Gets the current user's notification preferences.
     * Creates default preferences if none exist.
     */
    @Transactional
    public NotificationPreference getPreferences() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Gets notification preferences for a specific user (used internally by
     * listeners).
     */
    public NotificationPreference getPreferencesForUser(UUID userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Updates the current user's notification preferences.
     */
    @Transactional
    public NotificationPreference updatePreferences(NotificationPreferenceRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }
        if (request.getTaskAssigned() != null) {
            preference.setTaskAssigned(request.getTaskAssigned());
        }
        if (request.getTaskOverdue() != null) {
            preference.setTaskOverdue(request.getTaskOverdue());
        }
        if (request.getDailyDigest() != null) {
            preference.setDailyDigest(request.getDailyDigest());
        }
        if (request.getDigestTime() != null) {
            preference.setDigestTime(request.getDigestTime());
        }

        preference = preferenceRepository.save(preference);
        log.info("Updated notification preferences for user [{}]", userId);
        return preference;
    }

    private NotificationPreference createDefaultPreferences(UUID userId) {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .build();
        return preferenceRepository.save(preference);
    }
}

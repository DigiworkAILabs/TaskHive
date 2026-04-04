package com.digiwork.taskhive.module.notification.service;

import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.notification.dto.NotificationPreferenceRequest;
import com.digiwork.taskhive.module.notification.model.NotificationPreference;
import com.digiwork.taskhive.module.notification.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private NotificationPreferenceService preferenceService;

    private UUID userId;
    private NotificationPreference existingPreference;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingPreference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .taskAssigned(true)
                .taskOverdue(true)
                .build();

        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    @DisplayName("getPreferences should return existing preferences")
    void getPreferences_Existing() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existingPreference));

        NotificationPreference result = preferenceService.getPreferences();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPreferences should create default if none exist")
    void getPreferences_CreateDefault() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArgument(0));

        NotificationPreference result = preferenceService.getPreferences();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("getPreferencesForUser should return existing preferences")
    void getPreferencesForUser() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existingPreference));

        NotificationPreference result = preferenceService.getPreferencesForUser(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePreferences should update existing preferences")
    void updatePreferences() {
        NotificationPreferenceRequest request = new NotificationPreferenceRequest();
        request.setEmailEnabled(false);
        request.setTaskAssigned(false);

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existingPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArgument(0));

        NotificationPreference result = preferenceService.updatePreferences(request);

        assertThat(result.getEmailEnabled()).isFalse();
        assertThat(result.getTaskAssigned()).isFalse();
        assertThat(result.getInAppEnabled()).isTrue(); // Unchanged
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }
}

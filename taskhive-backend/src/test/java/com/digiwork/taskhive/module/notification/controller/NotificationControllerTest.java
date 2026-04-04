package com.digiwork.taskhive.module.notification.controller;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.notification.dto.NotificationPreferenceRequest;
import com.digiwork.taskhive.module.notification.dto.NotificationResponse;
import com.digiwork.taskhive.module.notification.model.NotificationPreference;
import com.digiwork.taskhive.module.notification.service.NotificationPreferenceService;
import com.digiwork.taskhive.module.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
class NotificationControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationPreferenceService preferenceService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UsernamePasswordAuthenticationToken getEmployeeAuth() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "employee@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("Authenticated user should get notifications")
    void authenticatedUserShouldGetNotifications() throws Exception {
        when(notificationService.getNotifications(anyInt(), anyInt())).thenReturn(PageResponse.<NotificationResponse>builder()
                .content(new ArrayList<>())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build());
        when(notificationService.getUnreadCount()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated user should get unread notifications")
    void authenticatedUserShouldGetUnreadNotifications() throws Exception {
        when(notificationService.getUnreadNotifications(anyInt(), anyInt())).thenReturn(PageResponse.<NotificationResponse>builder()
                .content(new ArrayList<>())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build());

        mockMvc.perform(get("/api/v1/notifications/unread")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated user should get unread count")
    void authenticatedUserShouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated user should mark notification as read")
    void authenticatedUserShouldMarkAsRead() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/notifications/" + id + "/read")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated user should mark all as read")
    void authenticatedUserShouldMarkAllAsRead() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/mark-all-read")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated user should get preferences")
    void authenticatedUserShouldGetPreferences() throws Exception {
        when(preferenceService.getPreferences()).thenReturn(new NotificationPreference());

        mockMvc.perform(get("/api/v1/notifications/preferences")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated user should update preferences")
    void authenticatedUserShouldUpdatePreferences() throws Exception {
        when(preferenceService.updatePreferences(any(NotificationPreferenceRequest.class))).thenReturn(new NotificationPreference());

        mockMvc.perform(put("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailEnabled\":true,\"inAppEnabled\":true}")
                .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }
}

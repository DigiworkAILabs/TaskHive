package com.digiwork.taskhive.module.auth.controller;

import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.auth.service.GdprService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GdprController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@org.springframework.context.annotation.Import(GdprControllerTest.SecurityTestConfig.class)
class GdprControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    @org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
    static class SecurityTestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GdprService gdprService;

    @MockitoBean
    private com.digiwork.taskhive.module.audit.service.AuditService auditService;

    // We also need to mock SecurityConfig and JwtAuthenticationFilter dependencies if they try to load,
    // but excludeFilters usually handles JwtAuthenticationFilter.

    private UsernamePasswordAuthenticationToken getAuthToken(UUID userId, String role) {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId, "test@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("GET /{userId}/data-export")
    class ExportUserData {

        @Test
        @DisplayName("ADMIN can export any user's data")
        void adminCanExportAnyUserData() throws Exception {
            UUID adminId = UUID.randomUUID();
            UUID targetUserId = UUID.randomUUID();

            when(gdprService.exportUserData(targetUserId)).thenReturn(null);

            mockMvc.perform(get("/api/v1/users/{userId}/data-export", targetUserId)
                            .with(authentication(getAuthToken(adminId, "ADMIN"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("User can export their own data")
        void userCanExportOwnData() throws Exception {
            UUID userId = UUID.randomUUID();

            when(gdprService.exportUserData(userId)).thenReturn(null);

            mockMvc.perform(get("/api/v1/users/{userId}/data-export", userId)
                            .with(authentication(getAuthToken(userId, "EMPLOYEE"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("User cannot export another user's data")
        void userCannotExportOtherUserData() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID targetUserId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/users/{userId}/data-export", targetUserId)
                            .with(authentication(getAuthToken(userId, "EMPLOYEE"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated user cannot export data")
        void unauthenticatedUserCannotExportData() throws Exception {
            mockMvc.perform(get("/api/v1/users/{userId}/data-export", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /{userId}/gdpr-delete")
    class AnonymizeUserData {

        @Test
        @DisplayName("ADMIN can anonymize any user")
        void adminCanAnonymizeAnyUser() throws Exception {
            UUID adminId = UUID.randomUUID();
            UUID targetUserId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/users/{userId}/gdpr-delete", targetUserId)
                            .with(csrf())
                            .with(authentication(getAuthToken(adminId, "ADMIN"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("User can anonymize their own data")
        void userCanAnonymizeOwnData() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/users/{userId}/gdpr-delete", userId)
                            .with(csrf())
                            .with(authentication(getAuthToken(userId, "EMPLOYEE"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("User cannot anonymize another user's data")
        void userCannotAnonymizeOtherUser() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID targetUserId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/users/{userId}/gdpr-delete", targetUserId)
                            .with(csrf())
                            .with(authentication(getAuthToken(userId, "EMPLOYEE"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated user cannot anonymize data")
        void unauthenticatedUserCannotAnonymizeData() throws Exception {
            mockMvc.perform(post("/api/v1/users/{userId}/gdpr-delete", UUID.randomUUID())
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }
}

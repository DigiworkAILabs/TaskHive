package com.digiwork.taskhive.common.config;

import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SecurityConfig — validates endpoint authorization rules,
 * security headers, and role-based access control using @WebMvcTest with a
 * minimal stub controller.
 *
 * This test does NOT require a database.
 */
@WebMvcTest(controllers = SecurityConfigTest.StubController.class)
@Import({SecurityConfig.class, SecurityConfigTest.TestConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.digiwork.taskhive.module.audit.service.AuditService auditService;

    // ─── Test Configuration ──────────────────────────────────────────────────

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:3000"));
            config.setAllowedMethods(List.of("*"));
            config.setAllowedHeaders(List.of("*"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(null, null, null) {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {
                    // No-op filter for tests — authentication is set directly via SecurityContext
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    /**
     * Minimal stub controller that mirrors the project's real endpoint protection patterns.
     */
    @RestController
    static class StubController {

        @GetMapping("/api/v1/auth/login")
        public ResponseEntity<String> publicLogin() {
            return ResponseEntity.ok("public");
        }

        @GetMapping("/api/v1/auth/forgot-password")
        public ResponseEntity<String> publicForgotPassword() {
            return ResponseEntity.ok("public");
        }

        @GetMapping("/api/v1/test-tasks/my-tasks")
        public ResponseEntity<String> authenticatedMyTasks() {
            return ResponseEntity.ok("authenticated");
        }

        @GetMapping("/api/v1/test-tasks")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<String> adminOnlyTasks() {
            return ResponseEntity.ok("admin-only");
        }

        @GetMapping("/api/v1/test-employees")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<String> adminOnlyEmployees() {
            return ResponseEntity.ok("admin-only");
        }

        @GetMapping("/actuator/health")
        public ResponseEntity<String> healthCheck() {
            return ResponseEntity.ok("healthy");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void authenticateAs(String role) {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId, "test@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Public Endpoints (no auth required)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/v1/auth/login is accessible without authentication")
        void loginEndpointIsPublic() throws Exception {
            clearAuth();
            mockMvc.perform(get("/api/v1/auth/login"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("public"));
        }

        @Test
        @DisplayName("GET /api/v1/auth/forgot-password is accessible without authentication")
        void forgotPasswordIsPublic() throws Exception {
            clearAuth();
            mockMvc.perform(get("/api/v1/auth/forgot-password"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Actuator health endpoint is accessible without authentication")
        void actuatorHealthIsPublic() throws Exception {
            clearAuth();
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Protected Endpoints (authentication required)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Protected Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /api/v1/test-tasks/my-tasks is blocked without authentication")
        void myTasksBlockedWithoutAuth() throws Exception {
            clearAuth();
            mockMvc.perform(get("/api/v1/test-tasks/my-tasks"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/test-tasks/my-tasks is accessible when authenticated")
        void myTasksAccessibleWhenAuthenticated() throws Exception {
            authenticateAs("EMPLOYEE");
            mockMvc.perform(get("/api/v1/test-tasks/my-tasks"))
                    .andExpect(status().isOk());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin-Only Endpoints (RBAC)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Admin-Only Endpoints")
    class AdminOnlyEndpoints {

        @Test
        @DisplayName("GET /api/v1/test-tasks as ADMIN → 200 OK")
        void tasksAccessibleForAdmin() throws Exception {
            authenticateAs("ADMIN");
            mockMvc.perform(get("/api/v1/test-tasks"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/test-tasks as EMPLOYEE → 403 FORBIDDEN")
        void tasksBlockedForEmployee() throws Exception {
            authenticateAs("EMPLOYEE");
            mockMvc.perform(get("/api/v1/test-tasks"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/test-employees as ADMIN → 200 OK")
        void employeesAccessibleForAdmin() throws Exception {
            authenticateAs("ADMIN");
            mockMvc.perform(get("/api/v1/test-employees"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/test-employees as EMPLOYEE → 403 FORBIDDEN")
        void employeesBlockedForEmployee() throws Exception {
            authenticateAs("EMPLOYEE");
            mockMvc.perform(get("/api/v1/test-employees"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/test-tasks without authentication → 401 UNAUTHORIZED")
        void tasksBlockedWithoutAuth() throws Exception {
            clearAuth();
            mockMvc.perform(get("/api/v1/test-tasks"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Security Headers
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Security Headers")
    class SecurityHeaders {

        @Test
        @DisplayName("X-Frame-Options: DENY is present")
        void xFrameOptionsDeny() throws Exception {
            mockMvc.perform(get("/api/v1/auth/login"))
                    .andExpect(header().string("X-Frame-Options", "DENY"));
        }

        @Test
        @DisplayName("Content-Security-Policy header is present")
        void contentSecurityPolicy() throws Exception {
            mockMvc.perform(get("/api/v1/auth/login"))
                    .andExpect(header().exists("Content-Security-Policy"));
        }

        @Test
        @DisplayName("Strict-Transport-Security header is present")
        void hstsHeader() throws Exception {
            mockMvc.perform(get("/api/v1/auth/login")
                    .secure(true))
                    .andExpect(header().exists("Strict-Transport-Security"));
        }

        @Test
        @DisplayName("Referrer-Policy header is present")
        void referrerPolicy() throws Exception {
            mockMvc.perform(get("/api/v1/auth/login"))
                    .andExpect(header().exists("Referrer-Policy"));
        }
    }
}

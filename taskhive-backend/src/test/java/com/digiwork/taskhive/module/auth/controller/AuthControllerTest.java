package com.digiwork.taskhive.module.auth.controller;

import com.digiwork.taskhive.common.constants.CookieConstants;
import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.common.util.CookieUtil;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.dto.*;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.auth.service.AccountActivationService;
import com.digiwork.taskhive.module.auth.service.AuthService;
import com.digiwork.taskhive.module.auth.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(AuthControllerTest.SecurityTestConfig.class)
class AuthControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/activate-account", 
                                   "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password", 
                                   "/api/v1/auth/refresh").permitAll()
                    .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)));
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AccountActivationService accountActivationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private AuditService auditService;

    private UsernamePasswordAuthenticationToken getAuthToken() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "test@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("POST /login")
    class LoginTests {
        @Test
        @DisplayName("Login with valid credentials → 200 OK with user data")
        void loginSuccess() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("Password123!");

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setUser(UserInfoResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .email("user@example.com")
                    .firstName("Test")
                    .lastName("User")
                    .role("EMPLOYEE")
                    .build());

            when(authService.login(any(), any())).thenReturn(loginResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.LOGIN_SUCCESS))
                    .andExpect(jsonPath("$.data.user.email").value("user@example.com"))
                    .andExpect(jsonPath("$.data.user.role").value("EMPLOYEE"));
        }

        @Test
        @DisplayName("Login with invalid body → 400 BAD REQUEST")
        void loginInvalid() throws Exception {
            LoginRequest request = new LoginRequest(); 
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /logout")
    class LogoutTests {
        @Test
        @DisplayName("Logout is accessible when authenticated → 200 OK")
        void logoutSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(authentication(getAuthToken()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.LOGOUT_SUCCESS));
        }
    }

    @Nested
    @DisplayName("POST /refresh")
    class RefreshTests {
        @Test
        @DisplayName("Refresh with cookie → 200 OK")
        void refreshWithCookie() throws Exception {
            when(cookieUtil.extractCookieValue(any(), any())).thenReturn("some-refresh-token");

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie(CookieConstants.REFRESH_TOKEN_COOKIE, "some-refresh-token"))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.TOKEN_REFRESHED));
        }

        @Test
        @DisplayName("Refresh without cookie → 400 BAD REQUEST")
        void refreshWithoutCookie() throws Exception {
            when(cookieUtil.extractCookieValue(any(), any())).thenReturn(null);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(MessageConstants.REFRESH_TOKEN_MISSING));
        }
    }

    @Nested
    @DisplayName("POST /activate-account")
    class ActivateAccountTests {
        @Test
        @DisplayName("Activate account → 200 OK")
        void activateAccount() throws Exception {
            ActivateAccountRequest request = new ActivateAccountRequest();
            request.setToken("token");
            request.setNewPassword("Password123!");

            mockMvc.perform(post("/api/v1/auth/activate-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.ACCOUNT_ACTIVATED));
        }
    }

    @Nested
    @DisplayName("POST /forgot-password")
    class ForgotPasswordTests {
        @Test
        @DisplayName("Forgot password → 200 OK")
        void forgotPassword() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("user@example.com");

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.PASSWORD_RESET_EMAIL_SENT));
        }
    }

    @Nested
    @DisplayName("POST /reset-password")
    class ResetPasswordTests {
        @Test
        @DisplayName("Reset password → 200 OK")
        void resetPassword() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("token");
            request.setNewPassword("Password123!");

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.PASSWORD_RESET_SUCCESS));
        }
    }

    @Nested
    @DisplayName("POST /change-password")
    class ChangePasswordTests {
        @Test
        @DisplayName("Change password when authenticated → 200 OK")
        void changePasswordAuthenticated() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("OldPassword123!");
            request.setNewPassword("NewPassword123!");

            mockMvc.perform(post("/api/v1/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authentication(getAuthToken()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MessageConstants.PASSWORD_CHANGED));
        }

        @Test
        @DisplayName("Change password without auth → 401 UNAUTHORIZED")
        void changePasswordUnauthenticated() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("OldPassword123!");
            request.setNewPassword("NewPassword123!");

            mockMvc.perform(post("/api/v1/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /me")
    class GetCurrentUserTests {
        @Test
        @DisplayName("Get current user when authenticated → 200 OK")
        void getCurrentUserAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                            .with(authentication(getAuthToken())))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Get current user without auth → 401 UNAUTHORIZED")
        void getCurrentUserUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

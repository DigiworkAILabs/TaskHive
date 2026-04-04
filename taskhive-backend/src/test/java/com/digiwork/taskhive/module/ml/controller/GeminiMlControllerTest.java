package com.digiwork.taskhive.module.ml.controller;

import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.ml.dto.TaskPriorityResponse;
import com.digiwork.taskhive.module.ml.service.GeminiService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GeminiMlController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
class GeminiMlControllerTest {

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
    private GeminiService geminiService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UsernamePasswordAuthenticationToken getAdminAuth() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "admin@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken getEmployeeAuth() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "employee@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("ADMIN should access Gemini priority prediction")
    void adminShouldAccessGeminiPriorityPrediction() throws Exception {
        when(geminiService.suggestPriority(any())).thenReturn(TaskPriorityResponse.builder().build());
        mockMvc.perform(post("/api/v1/ml/gemini/predict/task-priority")
                        .with(authentication(getAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskTitle\":\"Test Task\",\"description\":\"Test description\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should be forbidden from Gemini priority prediction")
    void employeeShouldBeForbiddenFromGeminiPriorityPrediction() throws Exception {
        mockMvc.perform(post("/api/v1/ml/gemini/predict/task-priority")
                        .with(authentication(getEmployeeAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskTitle\":\"Test Task\"}"))
                .andExpect(status().isForbidden());
    }
}

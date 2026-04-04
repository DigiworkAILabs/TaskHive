package com.digiwork.taskhive.module.analytics.controller;

import com.digiwork.taskhive.module.analytics.dto.AdminDashboardResponse;
import com.digiwork.taskhive.module.analytics.service.AnalyticsService;
import com.digiwork.taskhive.module.analytics.service.AnomalyDetectionService;
import com.digiwork.taskhive.module.analytics.service.ReportService;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import com.digiwork.taskhive.module.audit.service.AuditService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(
        controllers = AnalyticsController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
class AnalyticsControllerTest {

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
    private AnalyticsService analyticsService;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private AnomalyDetectionService anomalyDetectionService;
    
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
    @DisplayName("ADMIN should access admin dashboard")
    void adminShouldAccessAdminDashboard() throws Exception {
        when(analyticsService.getAdminDashboard()).thenReturn(new AdminDashboardResponse());
        mockMvc.perform(get("/api/v1/analytics/dashboard/admin")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should be forbidden from admin dashboard")
    void employeeShouldBeForbiddenFromAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard/admin")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE should access employee dashboard")
    void employeeShouldAccessEmployeeDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard/employee")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should access task distribution")
    void adminShouldAccessTaskDistribution() throws Exception {
        when(analyticsService.getTaskDistribution()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/analytics/tasks/distribution")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should be forbidden from task distribution")
    void employeeShouldBeForbiddenFromTaskDistribution() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/tasks/distribution")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN should access employee performance")
    void adminShouldAccessEmployeePerformance() throws Exception {
        when(analyticsService.getEmployeePerformance()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/analytics/employees/performance")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should access today overview")
    void adminShouldAccessTodayOverview() throws Exception {
        when(anomalyDetectionService.getActiveAlerts()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/analytics/dashboard/today")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should access anomalies")
    void adminShouldAccessAnomalies() throws Exception {
        when(anomalyDetectionService.getActiveAlerts()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/analytics/anomalies")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should access missed tasks")
    void adminShouldAccessMissedTasks() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/tasks/missed")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should access late patterns")
    void adminShouldAccessLatePatterns() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/tasks/late")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should be forbidden from exporting reports")
    void employeeShouldBeForbiddenFromExportingReports() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/reports/export")
                        .with(authentication(getEmployeeAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}

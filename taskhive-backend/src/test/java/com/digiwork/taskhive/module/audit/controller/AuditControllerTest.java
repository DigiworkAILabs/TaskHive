package com.digiwork.taskhive.module.audit.controller;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.audit.service.ComplianceReportService;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuditController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
class AuditControllerTest {

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
    private AuditService auditService;

    @MockitoBean
    private ComplianceReportService complianceReportService;

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
    @DisplayName("ADMIN should access audit logs")
    void adminShouldAccessAuditLogs() throws Exception {
        when(auditService.getAllLogs(anyInt(), anyInt())).thenReturn(new PageResponse<>());
        mockMvc.perform(get("/api/v1/audit/logs")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should be forbidden from audit logs")
    void employeeShouldBeForbiddenFromAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs")
                        .with(authentication(getEmployeeAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN should search audit logs")
    void adminShouldSearchAuditLogs() throws Exception {
        when(auditService.searchLogs(any(), anyInt(), anyInt())).thenReturn(new PageResponse<>());
        mockMvc.perform(get("/api/v1/audit/logs/search")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should get security events")
    void adminShouldGetSecurityEvents() throws Exception {
        when(auditService.getSecurityEvents(anyInt(), anyInt())).thenReturn(new PageResponse<>());
        mockMvc.perform(get("/api/v1/audit/security-events")
                        .with(authentication(getAdminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should get compliance report (JSON)")
    void adminShouldGetComplianceReportJson() throws Exception {
        when(complianceReportService.getUserAccessReport(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>());
        mockMvc.perform(get("/api/v1/audit/compliance/report")
                        .with(authentication(getAdminAuth()))
                .param("reportType", "USER_ACCESS")
                .param("format", "json"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN should get compliance report (CSV)")
    void adminShouldGetComplianceReportCsv() throws Exception {
        when(complianceReportService.getUserAccessReportCsv(any(), any())).thenReturn("csv_content");
        mockMvc.perform(get("/api/v1/audit/compliance/report")
                        .with(authentication(getAdminAuth()))
                .param("reportType", "USER_ACCESS")
                .param("format", "csv"))
                .andExpect(status().isOk());
    }
}

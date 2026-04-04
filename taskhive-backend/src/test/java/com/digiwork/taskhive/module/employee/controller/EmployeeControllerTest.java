package com.digiwork.taskhive.module.employee.controller;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.security.JwtAuthenticationFilter;
import com.digiwork.taskhive.module.employee.dto.*;
import com.digiwork.taskhive.module.employee.service.EmployeeSearchService;
import com.digiwork.taskhive.module.employee.service.EmployeeService;
import com.digiwork.taskhive.module.employee.service.ProfilePhotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EmployeeController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(EmployeeControllerTest.SecurityTestConfig.class)
class EmployeeControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
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
    private EmployeeService employeeService;

    @MockitoBean
    private EmployeeSearchService employeeSearchService;

    @MockitoBean
    private ProfilePhotoService profilePhotoService;

    @MockitoBean
    private AuditService auditService;

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

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployeeTests {
        @Test
        @DisplayName("Admin can create employee → 201 CREATED")
        void createEmployeeSuccess() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setEmail("john.doe@example.com");
            request.setDepartment("IT");

            when(employeeService.createEmployee(any())).thenReturn(new EmployeeResponse());

            mockMvc.perform(post("/api/v1/employees")
                            .with(authentication(getAdminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Employee cannot create employee → 403 FORBIDDEN")
        void createEmployeeForbidden() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setEmail("john.doe@example.com");

            mockMvc.perform(post("/api/v1/employees")
                            .with(authentication(getEmployeeAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees")
    class ListEmployeesTests {
        @Test
        @DisplayName("Admin can list employees → 200 OK")
        void listEmployeesSuccess() throws Exception {
            when(employeeService.listEmployees(any(EmployeeFilterRequest.class)))
                    .thenReturn(new PageResponse<>());

            mockMvc.perform(get("/api/v1/employees")
                            .with(authentication(getAdminAuth())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/employees/{id}")
    class UpdateEmployeeTests {
        @Test
        @DisplayName("Admin can update employee → 200 OK")
        void updateEmployeeSuccess() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateEmployeeRequest request = new UpdateEmployeeRequest();
            request.setFirstName("Jane");

            when(employeeService.updateEmployee(eq(id), any())).thenReturn(new EmployeeResponse());

            mockMvc.perform(put("/api/v1/employees/" + id)
                            .with(authentication(getAdminAuth()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employees/{id}")
    class DeleteEmployeeTests {
        @Test
        @DisplayName("Admin can delete employee → 200 OK")
        void deleteEmployeeSuccess() throws Exception {
            UUID id = UUID.randomUUID();
            mockMvc.perform(delete("/api/v1/employees/" + id)
                            .with(authentication(getAdminAuth()))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/employees/{id}/photo")
    class PhotoTests {
        @Test
        @DisplayName("Admin can upload photo → 200 OK")
        void uploadPhotoSuccess() throws Exception {
            UUID id = UUID.randomUUID();
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            when(profilePhotoService.uploadPhoto(eq(id), any())).thenReturn("http://photo.url");

            mockMvc.perform(multipart("/api/v1/employees/" + id + "/photo")
                            .file(file)
                            .with(authentication(getAdminAuth()))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }
}

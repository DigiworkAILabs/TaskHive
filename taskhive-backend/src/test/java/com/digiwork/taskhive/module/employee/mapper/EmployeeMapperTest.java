package com.digiwork.taskhive.module.employee.mapper;

import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.employee.dto.EmployeeListResponse;
import com.digiwork.taskhive.module.employee.dto.EmployeeResponse;
import com.digiwork.taskhive.module.employee.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeMapperTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private EmployeeMapper employeeMapper;

    private UUID employeeId;
    private UUID userId;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        employee = Employee.builder()
                .id(employeeId)
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("1234567890")
                .department("Engineering")
                .designation("Senior Developer")
                .managerId(null)
                .joinDate(LocalDate.now())
                .photoUrl("uploads/avatar.png")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("toEmployeeResponse: should map all fields")
    void toEmployeeResponse() {
        when(storageService.getUrl("uploads/avatar.png")).thenReturn("http://localhost:8080/uploads/avatar.png");

        EmployeeResponse response = employeeMapper.toEmployeeResponse(employee, "Jane Boss");

        assertThat(response.getId()).isEqualTo(employeeId.toString());
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getManagerName()).isEqualTo("Jane Boss");
        assertThat(response.getPhotoUrl()).isEqualTo("http://localhost:8080/uploads/avatar.png");
    }

    @Test
    @DisplayName("toEmployeeListResponse: should map required fields")
    void toEmployeeListResponse() {
        when(storageService.getUrl("uploads/avatar.png")).thenReturn("http://localhost:8080/uploads/avatar.png");

        EmployeeListResponse response = employeeMapper.toEmployeeListResponse(employee);

        assertThat(response.getId()).isEqualTo(employeeId.toString());
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getPhotoUrl()).isEqualTo("http://localhost:8080/uploads/avatar.png");
    }
}

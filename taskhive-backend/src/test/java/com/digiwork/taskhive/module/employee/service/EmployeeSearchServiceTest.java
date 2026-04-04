package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.employee.dto.EmployeeListResponse;
import com.digiwork.taskhive.module.employee.mapper.EmployeeMapper;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeSearchServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeSearchService employeeSearchService;

    private Employee employee;
    private EmployeeListResponse employeeListResponse;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .build();

        employeeListResponse = new EmployeeListResponse();
        employeeListResponse.setFirstName("John");
        employeeListResponse.setLastName("Doe");
    }

    @Test
    @DisplayName("searchEmployees: should return mapped PageResponse with correct metadata")
    void searchEmployees_Success() {
        String query = "John";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        
        List<Employee> content = List.of(employee);
        Page<Employee> employeePage = new PageImpl<>(content, pageable, 1);

        when(employeeRepository.searchEmployees(eq(query), any(Pageable.class))).thenReturn(employeePage);
        when(employeeMapper.toEmployeeListResponse(employee)).thenReturn(employeeListResponse);

        PageResponse<EmployeeListResponse> response = employeeSearchService.searchEmployees(query, page, size);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getFirstName()).isEqualTo("John");
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();

        verify(employeeRepository).searchEmployees(eq(query), any(Pageable.class));
    }
}

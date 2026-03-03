package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.AccountActivationTokenRepository;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import com.digiwork.taskhive.module.auth.security.CustomUserDetails;
import com.digiwork.taskhive.module.auth.service.TokenService;
import com.digiwork.taskhive.module.employee.dto.*;
import com.digiwork.taskhive.module.employee.exception.EmployeeAlreadyExistsException;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.employee.mapper.EmployeeMapper;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.employee.repository.EmployeeStatusHistoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeStatusHistoryRepository statusHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AccountActivationTokenRepository activationTokenRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmployeeService employeeService;

    private UUID currentUserId;
    private UUID employeeId;
    private Employee testEmployee;
    private User testUser;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        testEmployee = Employee.builder()
                .id(employeeId)
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("Engineering")
                .status("ACTIVE")
                .isDeleted(false)
                .build();

        testUser = User.builder()
                .id(testEmployee.getUserId())
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .build();

        ReflectionTestUtils.setField(employeeService, "activationTokenExpiryMs", 86400000L);

        setSecurityContext(currentUserId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(UUID userId) {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId, "admin@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createEmployee Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployeeTests {

        @Test
        @DisplayName("should throw EmployeeAlreadyExistsException when email exists")
        void shouldThrowException_whenDuplicateEmail() {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setEmail("existing@example.com");

            when(employeeRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(request))
                    .isInstanceOf(EmployeeAlreadyExistsException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // listEmployees Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listEmployees")
    class ListEmployeesTests {

        @Test
        @DisplayName("should return paginated employee list")
        void shouldListEmployeesWithPagination() {
            Page<Employee> page = new PageImpl<>(List.of(testEmployee));
            EmployeeListResponse listResponse = new EmployeeListResponse();

            when(employeeRepository.findAllWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);
            when(employeeMapper.toEmployeeListResponse(testEmployee)).thenReturn(listResponse);

            PageResponse<EmployeeListResponse> result = employeeService.listEmployees(null, null, null, null, 0, 10,
                    "firstName", "asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getEmployee Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getEmployee")
    class GetEmployeeTests {

        @Test
        @DisplayName("should return employee by ID")
        void shouldGetEmployeeById() {
            EmployeeResponse expectedResponse = new EmployeeResponse();

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(employeeMapper.toEmployeeResponse(eq(testEmployee), any()))
                    .thenReturn(expectedResponse);

            EmployeeResponse result = employeeService.getEmployee(employeeId);

            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when not found")
        void shouldThrowException_whenEmployeeNotFound() {
            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployee(employeeId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // updateEmployee Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("should update employee fields successfully")
        void shouldUpdateEmployeeSuccessfully() {
            UpdateEmployeeRequest request = new UpdateEmployeeRequest();
            request.setFirstName("Jane");
            request.setDepartment("HR");
            EmployeeResponse expectedResponse = new EmployeeResponse();

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
            when(userRepository.findByIdAndIsDeletedFalse(testEmployee.getUserId()))
                    .thenReturn(Optional.of(testUser));
            when(employeeMapper.toEmployeeResponse(eq(testEmployee), any()))
                    .thenReturn(expectedResponse);

            EmployeeResponse result = employeeService.updateEmployee(employeeId, request);

            assertThat(result).isEqualTo(expectedResponse);
            verify(employeeRepository).save(any(Employee.class));
            verify(eventPublisher).publishEvent(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // softDeleteEmployee Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("softDeleteEmployee")
    class SoftDeleteTests {

        @Test
        @DisplayName("should soft delete employee and associated user")
        void shouldSoftDeleteEmployee() {
            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(userRepository.findByIdAndIsDeletedFalse(testEmployee.getUserId()))
                    .thenReturn(Optional.of(testUser));

            employeeService.softDeleteEmployee(employeeId);

            assertThat(testEmployee.getIsDeleted()).isTrue();
            assertThat(testEmployee.getStatus()).isEqualTo("DELETED");
            verify(employeeRepository).save(testEmployee);
            verify(userRepository).save(testUser);
            verify(statusHistoryRepository).save(any());
            verify(eventPublisher).publishEvent(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // activateEmployee Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("activateEmployee")
    class ActivateTests {

        @Test
        @DisplayName("should activate employee and associated user")
        void shouldActivateEmployee() {
            testEmployee.setStatus("PENDING");
            EmployeeResponse expectedResponse = new EmployeeResponse();

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
            when(userRepository.findByIdAndIsDeletedFalse(testEmployee.getUserId()))
                    .thenReturn(Optional.of(testUser));
            when(employeeMapper.toEmployeeResponse(eq(testEmployee), any()))
                    .thenReturn(expectedResponse);

            EmployeeResponse result = employeeService.activateEmployee(employeeId);

            assertThat(testEmployee.getStatus()).isEqualTo("ACTIVE");
            assertThat(result).isEqualTo(expectedResponse);
            verify(statusHistoryRepository).save(any());
            verify(eventPublisher).publishEvent(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deactivateEmployee Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deactivateEmployee")
    class DeactivateTests {

        @Test
        @DisplayName("should deactivate employee and associated user")
        void shouldDeactivateEmployee() {
            EmployeeResponse expectedResponse = new EmployeeResponse();

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
            when(userRepository.findByIdAndIsDeletedFalse(testEmployee.getUserId()))
                    .thenReturn(Optional.of(testUser));
            when(employeeMapper.toEmployeeResponse(eq(testEmployee), any()))
                    .thenReturn(expectedResponse);

            EmployeeResponse result = employeeService.deactivateEmployee(employeeId);

            assertThat(testEmployee.getStatus()).isEqualTo("INACTIVE");
            assertThat(result).isEqualTo(expectedResponse);
            verify(statusHistoryRepository).save(any());
            verify(eventPublisher).publishEvent(any());
        }
    }
}

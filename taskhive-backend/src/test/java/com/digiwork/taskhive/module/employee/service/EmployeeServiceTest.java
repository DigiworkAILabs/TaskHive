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

                @Test
                @DisplayName("should throw EmployeeNotFoundException when manager not found")
                void shouldThrowException_whenManagerNotFound() {
                        UUID managerId = UUID.randomUUID();
                        CreateEmployeeRequest request = new CreateEmployeeRequest();
                        request.setEmail("new@example.com");
                        request.setManagerId(managerId);

                        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
                        when(employeeRepository.findByIdAndIsDeletedFalse(managerId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> employeeService.createEmployee(request))
                                .isInstanceOf(EmployeeNotFoundException.class)
                                .hasMessageContaining("Manager not found");
                }

                @Test
                @DisplayName("should create employee successfully with User and Token")
                void shouldCreateEmployeeSuccessfully() {
                        CreateEmployeeRequest request = new CreateEmployeeRequest();
                        request.setFirstName("New");
                        request.setLastName("User");
                        request.setEmail("new@example.com");

                        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
                        when(userRepository.save(any(User.class))).thenAnswer(i -> {
                                User u = i.getArgument(0);
                                u.setId(UUID.randomUUID());
                                return u;
                        });
                        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(com.digiwork.taskhive.module.auth.model.Role.builder().id(UUID.randomUUID()).build()));
                        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
                        when(employeeMapper.toEmployeeResponse(any(), any())).thenReturn(new EmployeeResponse());

                        EmployeeResponse result = employeeService.createEmployee(request);

                        assertThat(result).isNotNull();
                        verify(userRepository).save(any(User.class));
                        verify(userRoleRepository).save(any());
                        verify(activationTokenRepository).save(any());
                        verify(eventPublisher).publishEvent(any());
                }

                @Test
                @DisplayName("should throw RuntimeException when EMPLOYEE role is missing")
                void shouldThrowException_whenRoleMissing() {
                        CreateEmployeeRequest request = new CreateEmployeeRequest();
                        request.setEmail("new@example.com");

                        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
                        when(userRepository.save(any(User.class))).thenAnswer(i -> {
                                User u = i.getArgument(0);
                                u.setId(UUID.randomUUID());
                                return u;
                        });
                        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> employeeService.createEmployee(request))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("EMPLOYEE role not found");
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

            EmployeeFilterRequest filter = EmployeeFilterRequest.builder()
                    .page(0).size(10).sortBy("firstName").sortDir("asc")
                    .build();
            PageResponse<EmployeeListResponse> result = employeeService.listEmployees(filter);

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
        @DisplayName("should resolve manager name when manager exists")
        void shouldResolveManagerName() {
            Employee manager = Employee.builder().firstName("Boss").lastName("Man").build();
            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(testEmployee));
            testEmployee.setManagerId(UUID.randomUUID());
            when(employeeRepository.findByIdAndIsDeletedFalse(testEmployee.getManagerId())).thenReturn(Optional.of(manager));
            when(employeeMapper.toEmployeeResponse(any(), eq("Boss Man"))).thenReturn(new EmployeeResponse());

            employeeService.getEmployee(employeeId);

            verify(employeeMapper).toEmployeeResponse(any(), eq("Boss Man"));
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

                @Test
                @DisplayName("should sync names to User record during update")
                void shouldSyncNamesToUser() {
                        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
                        request.setFirstName("UpdatedFirstName");
                        request.setLastName("UpdatedLastName");

                        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                                .thenReturn(Optional.of(testEmployee));
                        when(userRepository.findByIdAndIsDeletedFalse(testEmployee.getUserId()))
                                .thenReturn(Optional.of(testUser));
                        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

                        employeeService.updateEmployee(employeeId, request);

                        assertThat(testUser.getFirstName()).isEqualTo("UpdatedFirstName");
                        assertThat(testUser.getLastName()).isEqualTo("UpdatedLastName");
                        verify(userRepository).save(testUser);
                }

                @Test
                @DisplayName("should throw exception when updating with invalid manager")
                void shouldThrowException_whenInvalidManager() {
                        UUID invalidManagerId = UUID.randomUUID();
                        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
                        request.setManagerId(invalidManagerId);

                        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(testEmployee));
                        when(employeeRepository.findByIdAndIsDeletedFalse(invalidManagerId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, request))
                                .isInstanceOf(EmployeeNotFoundException.class);
                }
                @Test
                @DisplayName("should handle null user record during update")
                void shouldHandleNullUserDuringUpdate() {
                        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
                        request.setFirstName("Jane");

                        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(testEmployee));
                        when(userRepository.findByIdAndIsDeletedFalse(any())).thenReturn(Optional.empty());
                        when(employeeRepository.save(any())).thenReturn(testEmployee);

                        employeeService.updateEmployee(employeeId, request);

                        verify(userRepository, never()).save(any(User.class));
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

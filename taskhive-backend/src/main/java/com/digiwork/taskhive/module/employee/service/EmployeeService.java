package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.enums.RoleType;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.AccountActivationToken;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.AccountActivationTokenRepository;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.auth.service.TokenService;
import com.digiwork.taskhive.module.employee.dto.*;
import com.digiwork.taskhive.module.employee.event.*;
import com.digiwork.taskhive.module.employee.exception.EmployeeAlreadyExistsException;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.employee.mapper.EmployeeMapper;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.model.EmployeeStatusHistory;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.employee.repository.EmployeeStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final AccountActivationTokenRepository activationTokenRepository;
    private final TokenService tokenService;
    private final EmployeeMapper employeeMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.auth.activation-token-expiry}")
    private long activationTokenExpiryMs;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // Check email uniqueness
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new EmployeeAlreadyExistsException("Employee with email " + request.getEmail() + " already exists");
        }

        // Validate manager if provided
        if (request.getManagerId() != null) {
            employeeRepository.findByIdAndIsDeletedFalse(request.getManagerId())
                    .orElseThrow(() -> new EmployeeNotFoundException(
                            "Manager not found with id: " + request.getManagerId()));
        }

        // 1. Create User record (PENDING, no password)
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(UserStatus.PENDING)
                .failedAttempts(0)
                .isDeleted(false)
                .build();
        user = userRepository.save(user);

        // 2. Assign EMPLOYEE role
        var employeeRole = roleRepository.findByName(RoleType.EMPLOYEE.name())
                .orElseThrow(() -> new RuntimeException("EMPLOYEE role not found. Check Flyway migration V2."));

        UserRole userRole = UserRole.builder()
                .userId(user.getId())
                .roleId(employeeRole.getId())
                .assignedBy(currentUserId)
                .build();
        userRoleRepository.save(userRole);

        // 3. Create Employee record
        Employee employee = Employee.builder()
                .userId(user.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .managerId(request.getManagerId())
                .joinDate(request.getJoinDate())
                .status("PENDING")
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();
        employee = employeeRepository.save(employee);

        // 4. Record status history
        recordStatusChange(employee.getId(), null, "PENDING", currentUserId, "Employee created");

        // 5. Generate activation token
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = tokenService.hashToken(rawToken);

        AccountActivationToken activationToken = AccountActivationToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(activationTokenExpiryMs * 1_000_000L))
                .used(false)
                .build();
        activationTokenRepository.save(activationToken);

        // 6. Publish event (EmailService will send activation email)
        eventPublisher.publishEvent(new EmployeeCreatedEvent(
                this, employee.getId(), user.getId(), request.getEmail(), request.getFirstName(), rawToken, currentUserId));

        log.info("Employee created: {} ({})", employee.getEmail(), employee.getId());

        return employeeMapper.toEmployeeResponse(employee, resolveManagerName(employee.getManagerId()));
    }

    // ─── LIST ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<EmployeeListResponse> listEmployees(EmployeeFilterRequest filter) {

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy() != null ? filter.getSortBy() : "firstName");

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Employee> employeePage = employeeRepository.findAllWithFilters(
                filter.getName(), filter.getEmail(), filter.getDepartment(), filter.getStatus(), pageable);

        return PageResponse.<EmployeeListResponse>builder()
                .content(employeePage.getContent().stream()
                        .map(employeeMapper::toEmployeeListResponse)
                        .toList())
                .page(employeePage.getNumber())
                .size(employeePage.getSize())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .build();
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID employeeId) {
        Employee employee = findEmployeeOrThrow(employeeId);
        return employeeMapper.toEmployeeResponse(employee, resolveManagerName(employee.getManagerId()));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public EmployeeResponse updateEmployee(UUID employeeId, UpdateEmployeeRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee employee = findEmployeeOrThrow(employeeId);

        // Update fields if provided
        if (request.getFirstName() != null)
            employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            employee.setLastName(request.getLastName());
        if (request.getPhone() != null)
            employee.setPhone(request.getPhone());
        if (request.getDepartment() != null)
            employee.setDepartment(request.getDepartment());
        if (request.getDesignation() != null)
            employee.setDesignation(request.getDesignation());
        if (request.getJoinDate() != null)
            employee.setJoinDate(request.getJoinDate());
        if (request.getManagerId() != null) {
            employeeRepository.findByIdAndIsDeletedFalse(request.getManagerId())
                    .orElseThrow(() -> new EmployeeNotFoundException(
                            "Manager not found with id: " + request.getManagerId()));
            employee.setManagerId(request.getManagerId());
        }

        employee.setUpdatedBy(currentUserId);
        employee = employeeRepository.save(employee);

        // Also update the User record names if changed
        User user = userRepository.findByIdAndIsDeletedFalse(employee.getUserId()).orElse(null);
        if (user != null) {
            boolean userUpdated = false;
            if (request.getFirstName() != null) {
                user.setFirstName(request.getFirstName());
                userUpdated = true;
            }
            if (request.getLastName() != null) {
                user.setLastName(request.getLastName());
                userUpdated = true;
            }
            if (userUpdated)
                userRepository.save(user);
        }

        eventPublisher.publishEvent(new EmployeeUpdatedEvent(this, employee.getId(), currentUserId));
        log.info("Employee updated: {}", employeeId);

        return employeeMapper.toEmployeeResponse(employee, resolveManagerName(employee.getManagerId()));
    }

    // ─── SOFT DELETE ──────────────────────────────────────────────────────────

    @Transactional
    public void softDeleteEmployee(UUID employeeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee employee = findEmployeeOrThrow(employeeId);

        String oldStatus = employee.getStatus();
        employee.setIsDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employee.setStatus("DELETED");
        employee.setUpdatedBy(currentUserId);
        employeeRepository.save(employee);

        // Also soft-delete the user account
        User user = userRepository.findByIdAndIsDeletedFalse(employee.getUserId()).orElse(null);
        if (user != null) {
            user.setIsDeleted(true);
            user.setDeletedAt(LocalDateTime.now());
            user.setStatus(UserStatus.DELETED);
            userRepository.save(user);
        }

        recordStatusChange(employee.getId(), oldStatus, "DELETED", currentUserId, "Employee deleted by admin");

        eventPublisher.publishEvent(new EmployeeDeletedEvent(this, employee.getId(), currentUserId));
        log.info("Employee soft-deleted: {}", employeeId);
    }

    // ─── ACTIVATE ─────────────────────────────────────────────────────────────

    @Transactional
    public EmployeeResponse activateEmployee(UUID employeeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee employee = findEmployeeOrThrow(employeeId);

        String oldStatus = employee.getStatus();
        employee.setStatus("ACTIVE");
        employee.setUpdatedBy(currentUserId);
        employee = employeeRepository.save(employee);

        // Also activate the user account
        User user = userRepository.findByIdAndIsDeletedFalse(employee.getUserId()).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        recordStatusChange(employee.getId(), oldStatus, "ACTIVE", currentUserId, "Activated by admin");

        eventPublisher.publishEvent(new EmployeeActivatedEvent(this, employee.getId(), currentUserId));
        log.info("Employee activated: {}", employeeId);

        return employeeMapper.toEmployeeResponse(employee, resolveManagerName(employee.getManagerId()));
    }

    // ─── DEACTIVATE ───────────────────────────────────────────────────────────

    @Transactional
    public EmployeeResponse deactivateEmployee(UUID employeeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee employee = findEmployeeOrThrow(employeeId);

        String oldStatus = employee.getStatus();
        employee.setStatus("INACTIVE");
        employee.setUpdatedBy(currentUserId);
        employee = employeeRepository.save(employee);

        // Also deactivate the user account
        User user = userRepository.findByIdAndIsDeletedFalse(employee.getUserId()).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
        }

        recordStatusChange(employee.getId(), oldStatus, "INACTIVE", currentUserId, "Deactivated by admin");

        eventPublisher.publishEvent(new EmployeeDeactivatedEvent(this, employee.getId(), currentUserId));
        log.info("Employee deactivated: {}", employeeId);

        return employeeMapper.toEmployeeResponse(employee, resolveManagerName(employee.getManagerId()));
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Employee findEmployeeOrThrow(UUID employeeId) {
        return employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
    }

    private String resolveManagerName(UUID managerId) {
        if (managerId == null)
            return null;
        return employeeRepository.findByIdAndIsDeletedFalse(managerId)
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .orElse(null);
    }

    private void recordStatusChange(UUID employeeId, String oldStatus, String newStatus, UUID changedBy,
            String reason) {
        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employeeId(employeeId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .reason(reason)
                .build();
        statusHistoryRepository.save(history);
    }
}

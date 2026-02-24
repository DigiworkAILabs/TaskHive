package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.enums.TaskPriority;
import com.digiwork.taskhive.module.task.enums.TaskStatus;
import com.digiwork.taskhive.module.task.event.*;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskStatusHistory;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import com.digiwork.taskhive.module.task.repository.TaskStatusHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository statusHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // Validate priority
        try {
            TaskPriority.valueOf(request.getPriority());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid priority: " + request.getPriority()
                    + ". Allowed values: LOW, MEDIUM, HIGH, CRITICAL");
        }

        // Validate due date is in the future
        if (request.getDueDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Due date must be in the future");
        }

        // Validate assignee is an active employee
        Employee assignee = employeeRepository.findByIdAndIsDeletedFalse(request.getAssignedTo())
                .orElseThrow(() -> new BusinessException("Employee not found with id: " + request.getAssignedTo()));

        if (!"ACTIVE".equals(assignee.getStatus())) {
            throw new BusinessException("Task can only be assigned to an active employee");
        }

        // Build task entity
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO.name())
                .priority(request.getPriority())
                .assignedTo(request.getAssignedTo())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .tags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        task = taskRepository.save(task);

        // Record initial status history
        recordStatusChange(task.getId(), null, TaskStatus.TODO.name(), currentUserId, "Task created", null);

        // Publish events
        eventPublisher.publishEvent(new TaskCreatedEvent(this, task.getId(), currentUserId));
        eventPublisher.publishEvent(new TaskAssignedEvent(
                this, task.getId(), request.getAssignedTo(), task.getTitle(), currentUserId));

        log.info("Task created: {} ({})", task.getTitle(), task.getId());

        return taskMapper.toTaskResponse(task);
    }

    // ─── GET ALL TASKS (ADMIN) ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getAllTasks(
            String status, String priority, UUID assignedTo,
            LocalDateTime dueDateFrom, LocalDateTime dueDateTo,
            int page, int size, String sortBy, String sortDir) {

        String dbColumn = mapToColumnName(sortBy != null ? sortBy : "createdAt");

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                dbColumn);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findAllWithFilters(
                status, priority, assignedTo, dueDateFrom, dueDateTo, pageable);

        return PageResponse.<TaskListResponse>builder()
                .content(taskPage.getContent().stream()
                        .map(taskMapper::toTaskListResponse)
                        .toList())
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    private String mapToColumnName(String sortBy) {
        return switch (sortBy) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            case "dueDate" -> "due_date";
            case "completedAt" -> "completed_at";
            case "assignedTo" -> "assigned_to";
            case "estimatedHours" -> "estimated_hours";
            default -> sortBy;
        };
    }

    // ─── GET TASK BY ID ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();

        Task task = findTaskOrThrow(taskId);

        // EMPLOYEE can only view their own assigned tasks
        if ("EMPLOYEE".equals(currentRole)) {
            Employee employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new TaskAccessDeniedException("Employee record not found"));
            if (!task.getAssignedTo().equals(employee.getId())) {
                throw new TaskAccessDeniedException("You can only view tasks assigned to you");
            }
        }

        return taskMapper.toTaskResponse(task);
    }

    // ─── UPDATE TASK (ADMIN) ──────────────────────────────────────────────────

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Task task = findTaskOrThrow(taskId);

        UUID oldAssignee = task.getAssignedTo();

        // Update fields if provided
        if (request.getTitle() != null)
            task.setTitle(request.getTitle());
        if (request.getDescription() != null)
            task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            try {
                TaskPriority.valueOf(request.getPriority());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid priority: " + request.getPriority());
            }
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());
        if (request.getEstimatedHours() != null)
            task.setEstimatedHours(request.getEstimatedHours());
        if (request.getTags() != null)
            task.setTags(request.getTags().toArray(new String[0]));

        if (request.getAssignedTo() != null) {
            Employee newAssignee = employeeRepository.findByIdAndIsDeletedFalse(request.getAssignedTo())
                    .orElseThrow(() -> new BusinessException("Employee not found with id: " + request.getAssignedTo()));
            if (!"ACTIVE".equals(newAssignee.getStatus())) {
                throw new BusinessException("Task can only be assigned to an active employee");
            }
            task.setAssignedTo(request.getAssignedTo());
        }

        task.setUpdatedBy(currentUserId);
        task = taskRepository.save(task);

        // Publish events
        eventPublisher.publishEvent(new TaskUpdatedEvent(this, task.getId(), currentUserId));

        // If assignee changed, publish reassignment event
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(oldAssignee)) {
            eventPublisher.publishEvent(new TaskAssignedEvent(
                    this, task.getId(), request.getAssignedTo(), task.getTitle(), currentUserId));
        }

        log.info("Task updated: {}", taskId);

        return taskMapper.toTaskResponse(task);
    }

    // ─── SOFT DELETE (ADMIN) ──────────────────────────────────────────────────

    @Transactional
    public void softDeleteTask(UUID taskId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Task task = findTaskOrThrow(taskId);

        task.setIsDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        task.setUpdatedBy(currentUserId);
        taskRepository.save(task);

        log.info("Task soft-deleted: {}", taskId);
    }

    // ─── UPDATE STATUS ────────────────────────────────────────────────────────

    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, UpdateTaskStatusRequest request, HttpServletRequest httpRequest) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();

        Task task = findTaskOrThrow(taskId);

        // EMPLOYEE can only update status of their own tasks
        if ("EMPLOYEE".equals(currentRole)) {
            Employee employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new TaskAccessDeniedException("Employee record not found"));
            if (!task.getAssignedTo().equals(employee.getId())) {
                throw new TaskAccessDeniedException("You can only update the status of tasks assigned to you");
            }
        }

        // Validate status transition
        TaskStatus currentStatus;
        try {
            currentStatus = TaskStatus.valueOf(task.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Current task status is invalid: " + task.getStatus());
        }

        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + request.getStatus()
                    + ". Allowed values: TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED");
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        String oldStatus = task.getStatus();
        task.setStatus(newStatus.name());

        // Set completedAt when transitioning to DONE
        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        task.setUpdatedBy(currentUserId);
        task = taskRepository.save(task);

        // Record status history
        String ipAddress = httpRequest != null ? httpRequest.getRemoteAddr() : null;
        recordStatusChange(task.getId(), oldStatus, newStatus.name(), currentUserId, request.getComment(), ipAddress);

        // Publish event
        eventPublisher.publishEvent(new TaskStatusChangedEvent(
                this, task.getId(), oldStatus, newStatus.name(), currentUserId));

        log.info("Task {} status changed: {} → {}", taskId, oldStatus, newStatus);

        return taskMapper.toTaskResponse(task);
    }

    // ─── MY TASKS (EMPLOYEE) ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getMyTasks(int page, int size, String sortBy, String sortDir) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // Find employee record for current user
        Employee employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new BusinessException("Employee record not found for current user"));

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "dueDate");

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findByAssignedToAndIsDeletedFalse(employee.getId(), pageable);

        return PageResponse.<TaskListResponse>builder()
                .content(taskPage.getContent().stream()
                        .map(taskMapper::toTaskListResponse)
                        .toList())
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    // ─── OVERDUE TASKS ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getOverdueTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueDate"));
        Page<Task> taskPage = taskRepository.findOverdueTasksPaged(LocalDateTime.now(), pageable);

        return PageResponse.<TaskListResponse>builder()
                .content(taskPage.getContent().stream()
                        .map(taskMapper::toTaskListResponse)
                        .toList())
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Task findTaskOrThrow(UUID taskId) {
        return taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
    }

    private void recordStatusChange(UUID taskId, String oldStatus, String newStatus,
            UUID changedBy, String comment, String ipAddress) {
        TaskStatusHistory history = TaskStatusHistory.builder()
                .taskId(taskId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .comment(comment)
                .ipAddress(ipAddress)
                .build();
        statusHistoryRepository.save(history);
    }
}

package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.enums.AttachmentPurpose;
import com.digiwork.taskhive.module.task.enums.TaskPriority;
import com.digiwork.taskhive.module.task.enums.TaskStatus;
import com.digiwork.taskhive.module.task.event.*;
import com.digiwork.taskhive.module.task.exception.ProofEnforcementException;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskStatusHistory;
import com.digiwork.taskhive.module.task.repository.TaskAttachmentRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository statusHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskAttachmentRepository attachmentRepository;
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
                .proofRequired(request.isProofRequired())       // P1.1
                .approvalRequired(request.isApprovalRequired()) // P1.2
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
        if (request.getProofRequired() != null)          // P1.1
            task.setProofRequired(request.getProofRequired());
        if (request.getApprovalRequired() != null)       // P1.2
            task.setApprovalRequired(request.getApprovalRequired());

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

        eventPublisher.publishEvent(new TaskUpdatedEvent(this, task.getId(), currentUserId));

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

        // Validate current status
        TaskStatus currentStatus;
        try {
            currentStatus = TaskStatus.valueOf(task.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Current task status is invalid: " + task.getStatus());
        }

        // Validate requested status
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + request.getStatus()
                    + ". Allowed values: TODO, IN_PROGRESS, IN_REVIEW, PENDING_APPROVAL, DONE, CANCELLED");
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // ── P1.3: Mandatory cancel reason ────────────────────────────────────
        validateCancelReason(newStatus, request.getReason());

        // ── P1.1: Proof enforcement ───────────────────────────────────────────
        enforceProofIfRequired(task, newStatus);

        // ── P1.4: Late tracking + submittedAt ────────────────────────────────
        handleSubmission(task, newStatus);

        // ── P1.2: Auto-advance to PENDING_APPROVAL when approval required ─────
        TaskStatus actualStatus = resolveActualStatus(task, newStatus);

        String oldStatus = task.getStatus();
        task.setStatus(actualStatus.name());

        if (actualStatus == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        task.setUpdatedBy(currentUserId);
        task = taskRepository.save(task);

        // Record status history — use reason as comment for cancellations
        String historyComment = (newStatus == TaskStatus.CANCELLED && request.getReason() != null)
                ? request.getReason()
                : request.getComment();
        String ipAddress = httpRequest != null ? httpRequest.getRemoteAddr() : null;
        recordStatusChange(task.getId(), oldStatus, actualStatus.name(), currentUserId, historyComment, ipAddress);

        eventPublisher.publishEvent(new TaskStatusChangedEvent(
                this, task.getId(), oldStatus, actualStatus.name(), currentUserId));

        log.info("Task {} status changed: {} → {}", taskId, oldStatus, actualStatus);
        return taskMapper.toTaskResponse(task);
    }

    // ─── APPROVE TASK (ADMIN) ─────────────────────────────────────────────────

    @Transactional
    public TaskResponse approveTask(UUID taskId, UUID adminUserId) {
        Task task = findTaskOrThrow(taskId);

        if (!TaskStatus.PENDING_APPROVAL.name().equals(task.getStatus())) {
            throw new BusinessException("TASK_3002", "Task must be in PENDING_APPROVAL status to approve");
        }

        String oldStatus = task.getStatus();
        task.setStatus(TaskStatus.DONE.name());
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedBy(adminUserId);
        task = taskRepository.save(task);

        recordStatusChange(task.getId(), oldStatus, TaskStatus.DONE.name(), adminUserId,
                "Task approved by admin", null);

        eventPublisher.publishEvent(new TaskApprovedEvent(
                this, task.getId(), task.getAssignedTo(), adminUserId, task.getTitle()));
        eventPublisher.publishEvent(new TaskStatusChangedEvent(
                this, task.getId(), oldStatus, TaskStatus.DONE.name(), adminUserId));

        log.info("Task {} approved by admin {}", taskId, adminUserId);
        return taskMapper.toTaskResponse(task);
    }

    // ─── REJECT TASK (ADMIN) ──────────────────────────────────────────────────

    @Transactional
    public TaskResponse rejectTask(UUID taskId, UUID adminUserId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessException("TASK_3005", "Rejection reason is mandatory");
        }

        Task task = findTaskOrThrow(taskId);

        if (!TaskStatus.PENDING_APPROVAL.name().equals(task.getStatus())) {
            throw new BusinessException("TASK_3002", "Task must be in PENDING_APPROVAL status to reject");
        }

        String oldStatus = task.getStatus();
        task.setStatus(TaskStatus.IN_REVIEW.name());
        task.setUpdatedBy(adminUserId);
        task = taskRepository.save(task);

        recordStatusChange(task.getId(), oldStatus, TaskStatus.IN_REVIEW.name(), adminUserId,
                rejectionReason, null);

        eventPublisher.publishEvent(new TaskRejectedEvent(
                this, task.getId(), task.getAssignedTo(), adminUserId, task.getTitle(), rejectionReason));
        eventPublisher.publishEvent(new TaskStatusChangedEvent(
                this, task.getId(), oldStatus, TaskStatus.IN_REVIEW.name(), adminUserId));

        log.info("Task {} rejected by admin {}: {}", taskId, adminUserId, rejectionReason);
        return taskMapper.toTaskResponse(task);
    }

    // ─── LATE TASKS (ADMIN) ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskListResponse> getLateTasks() {
        return taskRepository.findByIsLateTrue().stream()
                .map(taskMapper::toTaskListResponse)
                .toList();
    }

    // ─── MY TASKS (EMPLOYEE) ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getMyTasks(
            int page, int size, String sortBy, String sortDir,
            String status, String priority) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        Employee employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new BusinessException("Employee record not found for current user"));

        // Map frontend camelCase field names to entity field names (Spring Data JPA uses entity fields)
        String entityField = switch (sortBy != null ? sortBy : "dueDate") {
            case "dueDate" -> "dueDate";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            case "status" -> "status";
            case "priority" -> "priority";
            case "title" -> "title";
            default -> "dueDate";
        };

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                entityField);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findMyTasksWithFilters(
                employee.getId(), status, priority, pageable);

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

    // ─── P1 PRIVATE HELPERS ───────────────────────────────────────────────────

    /**
     * P1.3 — Mandatory cancel reason. Error code TASK_3004.
     */
    private void validateCancelReason(TaskStatus newStatus, String reason) {
        if (newStatus == TaskStatus.CANCELLED && (reason == null || reason.isBlank())) {
            throw new BusinessException("TASK_3004", "Reason is mandatory when cancelling a task");
        }
    }

    /**
     * P1.1 — Block IN_REVIEW (or DONE as safety net) if proof_required = true
     * and no PROOF attachment exists. Error code TASK_3003.
     */
    private void enforceProofIfRequired(Task task, TaskStatus newStatus) {
        boolean isSubmission = (newStatus == TaskStatus.IN_REVIEW || newStatus == TaskStatus.DONE);
        if (isSubmission && Boolean.TRUE.equals(task.getProofRequired())) {
            boolean hasProof = attachmentRepository
                    .existsByTaskIdAndAttachmentPurpose(task.getId(), AttachmentPurpose.PROOF);
            if (!hasProof) {
                throw new ProofEnforcementException();
            }
        }
    }

    /**
     * P1.4 — Set submittedAt and late flag when moving to IN_REVIEW.
     * is_late is immutable once set.
     */
    private void handleSubmission(Task task, TaskStatus newStatus) {
        if (newStatus == TaskStatus.IN_REVIEW) {
            LocalDateTime now = LocalDateTime.now();
            task.setSubmittedAt(now);

            // Only set once — immutable after first flag
            if (!Boolean.TRUE.equals(task.getIsLate()) && task.getDueDate() != null) {
                if (now.isAfter(task.getDueDate())) {
                    task.setIsLate(true);
                    long minutes = ChronoUnit.MINUTES.between(task.getDueDate(), now);
                    task.setLateByMinutes((int) minutes);
                }
            }
        }
    }

    /**
     * P1.2 — Auto-advance to PENDING_APPROVAL when approval_required = true.
     * Intercepts IN_REVIEW (normal path) and DONE (safety net for API bypass).
     */
    private TaskStatus resolveActualStatus(Task task, TaskStatus requestedStatus) {
        if (Boolean.TRUE.equals(task.getApprovalRequired()) &&
                (requestedStatus == TaskStatus.IN_REVIEW || requestedStatus == TaskStatus.DONE)) {
            return TaskStatus.PENDING_APPROVAL;
        }
        return requestedStatus;
    }

    // ─── SHARED HELPERS ───────────────────────────────────────────────────────

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

package com.digiwork.taskhive.module.task.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.*;
import com.digiwork.taskhive.module.task.service.TaskSearchService;
import com.digiwork.taskhive.module.task.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskSearchService taskSearchService;
    private final EmployeeRepository employeeRepository;

    // ─── ADMIN: Create Task ───────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    // ─── ADMIN: Get All Tasks ─────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TaskListResponse>>> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<TaskListResponse> tasks = taskService.getAllTasks(
                status, priority, assignedTo, dueDateFrom, dueDateTo, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    // ─── Authenticated: My Tasks ──────────────────────────────────────────────

    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse<PageResponse<TaskListResponse>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        PageResponse<TaskListResponse> tasks = taskService.getMyTasks(page, size, sortBy, sortDir, status, priority);
        return ResponseEntity.ok(ApiResponse.success("My tasks retrieved successfully", tasks));
    }

    // ─── ADMIN: Overdue Tasks ─────────────────────────────────────────────────

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TaskListResponse>>> getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TaskListResponse> tasks = taskService.getOverdueTasks(page, size);
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks retrieved successfully", tasks));
    }

    // ─── Authenticated: Search Tasks ──────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TaskListResponse>>> searchTasks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID assignedTo = null;
        String currentRole = SecurityUtils.getCurrentUserRole();

        if ("EMPLOYEE".equals(currentRole)) {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            Employee employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new BusinessException("Employee record not found"));
            assignedTo = employee.getId();
        }

        PageResponse<TaskListResponse> tasks = taskSearchService.searchTasks(query, assignedTo, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", tasks));
    }

    // ─── Authenticated: Get Task by ID ────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable UUID id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
    }

    // ─── ADMIN: Update Task ───────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    // ─── ADMIN: Delete Task ───────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        taskService.softDeleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    // ─── Authenticated: Update Task Status ────────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            HttpServletRequest httpRequest) {
        TaskResponse task = taskService.updateTaskStatus(id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", task));
    }

    // ─── ADMIN: Approve Task ──────────────────────────────────────────────────

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> approveTask(@PathVariable UUID id) {
        UUID adminUserId = SecurityUtils.getCurrentUserId();
        TaskResponse task = taskService.approveTask(id, adminUserId);
        return ResponseEntity.ok(ApiResponse.success("Task approved successfully", task));
    }

    // ─── ADMIN: Reject Task ───────────────────────────────────────────────────

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> rejectTask(
            @PathVariable UUID id,
            @RequestBody TaskApprovalRequest request) {
        UUID adminUserId = SecurityUtils.getCurrentUserId();
        TaskResponse task = taskService.rejectTask(id, adminUserId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Task rejected and returned for revision", task));
    }

    // ─── ADMIN: Late Tasks ────────────────────────────────────────────────────

    @GetMapping("/late")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<TaskListResponse>>> getLateTasks() {
        return ResponseEntity.ok(ApiResponse.success("Late tasks retrieved successfully",
                taskService.getLateTasks()));
    }
}


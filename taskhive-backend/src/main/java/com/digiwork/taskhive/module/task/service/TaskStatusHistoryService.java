package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.TaskStatusHistoryResponse;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import com.digiwork.taskhive.module.task.repository.TaskStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskStatusHistoryService {

    private final TaskStatusHistoryRepository historyRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryResponse> getHistory(UUID taskId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();

        // Verify task exists
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        // EMPLOYEE can only view history of their assigned tasks
        if ("EMPLOYEE".equals(currentRole)) {
            var employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new TaskAccessDeniedException("Employee record not found"));
            if (!task.getAssignedTo().equals(employee.getId())) {
                throw new TaskAccessDeniedException("You can only view history of tasks assigned to you");
            }
        }

        return historyRepository.findByTaskIdOrderByChangedAtDesc(taskId).stream()
                .map(taskMapper::toTaskStatusHistoryResponse)
                .toList();
    }
}

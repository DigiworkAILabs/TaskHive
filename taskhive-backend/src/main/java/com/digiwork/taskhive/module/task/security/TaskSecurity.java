package com.digiwork.taskhive.module.task.security;

import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public boolean canAccessTask(UUID taskId) {
        String currentRole = SecurityUtils.getCurrentUserRole();

        if ("ADMIN".equals(currentRole)) {
            return true;
        }

        if ("EMPLOYEE".equals(currentRole)) {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            return taskRepository.findByIdAndIsDeletedFalse(taskId)
                    .flatMap(t -> employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                            .map(e -> t.getAssignedTo() != null && t.getAssignedTo().equals(e.getId())))
                    .orElse(false);
        }

        return false;
    }
}

package com.digiwork.taskhive.module.notification.listener;

import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.auth.repository.UserRoleRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.notification.enums.NotificationType;
import com.digiwork.taskhive.module.notification.service.EmailQueueService;
import com.digiwork.taskhive.module.notification.service.NotificationService;
import com.digiwork.taskhive.module.task.event.TaskAssignedEvent;
import com.digiwork.taskhive.module.task.event.TaskCommentAddedEvent;
import com.digiwork.taskhive.module.task.event.TaskOverdueEvent;
import com.digiwork.taskhive.module.task.event.TaskStatusChangedEvent;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final NotificationService notificationService;
    private final EmailQueueService emailQueueService;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    // ─── Helper: get all admin user IDs ──────────────────────────────────────

    private List<UUID> getAdminUserIds() {
        return userRoleRepository.findUserIdsByRoleName("ADMIN");
    }

    // ─── Task Assigned ───────────────────────────────────────────────────────

    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Handling TaskAssignedEvent for task [{}] assigned to employee [{}]",
                event.getTaskId(), event.getEmployeeId());

        // Resolve employee details for notification
        Optional<Employee> employeeOpt = employeeRepository.findByIdAndIsDeletedFalse(event.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            log.warn("Employee [{}] not found, skipping notification", event.getEmployeeId());
            return;
        }
        Employee employee = employeeOpt.get();
        String employeeName = employee.getFirstName() + " " + employee.getLastName();

        // Create in-app notification for employee
        notificationService.createNotification(
                employee.getUserId(),
                NotificationType.TASK_ASSIGNED,
                "New Task Assigned",
                "You have been assigned a new task: " + event.getTaskTitle(),
                "TASK",
                event.getTaskId());

        // Queue task-assigned email for employee
        Optional<Task> taskOpt = taskRepository.findById(event.getTaskId());
        String priority = taskOpt.map(Task::getPriority).orElse("MEDIUM");
        String dueDate = taskOpt.map(t -> t.getDueDate().toLocalDate().toString()).orElse("N/A");

        emailQueueService.queueEmail(
                employee.getEmail(),
                "New Task Assigned - TaskHive",
                "task-assigned",
                Map.of(
                        "employeeName", employeeName,
                        "taskTitle", event.getTaskTitle(),
                        "priority", priority,
                        "dueDate", dueDate));
    }

    // ─── Task Status Changed ─────────────────────────────────────────────────

    @EventListener
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.info("Handling TaskStatusChangedEvent for task [{}]: {} → {}",
                event.getTaskId(), event.getOldStatus(), event.getNewStatus());

        // Resolve the task to find assigned employee
        Optional<Task> taskOpt = taskRepository.findById(event.getTaskId());
        if (taskOpt.isEmpty()) {
            log.warn("Task [{}] not found, skipping notification", event.getTaskId());
            return;
        }
        Task task = taskOpt.get();

        // Resolve the assigned employee
        Optional<Employee> employeeOpt = employeeRepository.findByIdAndIsDeletedFalse(task.getAssignedTo());
        if (employeeOpt.isEmpty()) {
            log.warn("Assigned employee [{}] not found, skipping notification", task.getAssignedTo());
            return;
        }
        Employee employee = employeeOpt.get();

        // Notify the assigned employee (in-app)
        notificationService.createNotification(
                employee.getUserId(),
                NotificationType.TASK_STATUS_CHANGED,
                "Task Status Updated",
                "Task \"" + task.getTitle() + "\" status changed from " +
                        event.getOldStatus() + " to " + event.getNewStatus(),
                "TASK",
                event.getTaskId());

        // ─── Admin notifications ─────────────────────────────────────────
        boolean isCompleted = "COMPLETED".equalsIgnoreCase(event.getNewStatus());
        String employeeName = employee.getFirstName() + " " + employee.getLastName();

        for (UUID adminUserId : getAdminUserIds()) {
            // Skip if admin is the one who changed the status (avoid self-notification)
            if (adminUserId.equals(event.getChangedBy()))
                continue;

            if (isCompleted) {
                // Task COMPLETED → admin gets in-app + email
                notificationService.createNotification(
                        adminUserId,
                        NotificationType.TASK_COMPLETED,
                        "Task Completed",
                        "Task \"" + task.getTitle() + "\" has been completed by " + employeeName,
                        "TASK",
                        event.getTaskId());

                // Queue completion email to admin
                Optional<User> adminUser = userRepository.findByIdAndIsDeletedFalse(adminUserId);
                adminUser.ifPresent(admin -> {
                    String dueDate = task.getDueDate() != null
                            ? task.getDueDate().toLocalDate().toString()
                            : "N/A";
                    emailQueueService.queueEmail(
                            admin.getEmail(),
                            "Task Completed - TaskHive",
                            "task-assigned",
                            Map.of(
                                    "employeeName", admin.getFirstName() + " " + admin.getLastName(),
                                    "taskTitle", task.getTitle() + " — Completed by " + employeeName,
                                    "priority", task.getPriority() != null ? task.getPriority() : "MEDIUM",
                                    "dueDate", dueDate));
                });
            } else {
                // Other status changes → admin gets in-app only
                notificationService.createNotification(
                        adminUserId,
                        NotificationType.TASK_STATUS_CHANGED,
                        "Task Status Updated",
                        "Task \"" + task.getTitle() + "\" status changed from " +
                                event.getOldStatus() + " to " + event.getNewStatus() +
                                " (by " + employeeName + ")",
                        "TASK",
                        event.getTaskId());
            }
        }
    }

    // ─── Task Comment Added ──────────────────────────────────────────────────

    @EventListener
    public void handleTaskCommentAdded(TaskCommentAddedEvent event) {
        log.info("Handling TaskCommentAddedEvent for task [{}]", event.getTaskId());

        // Resolve the task to find assigned employee
        Optional<Task> taskOpt = taskRepository.findById(event.getTaskId());
        if (taskOpt.isEmpty()) {
            log.warn("Task [{}] not found, skipping notification", event.getTaskId());
            return;
        }
        Task task = taskOpt.get();

        // Resolve the assigned employee's userId
        Optional<Employee> employeeOpt = employeeRepository.findByIdAndIsDeletedFalse(task.getAssignedTo());
        if (employeeOpt.isEmpty()) {
            log.warn("Assigned employee [{}] not found, skipping notification", task.getAssignedTo());
            return;
        }

        // Don't notify the comment author themselves (employee side)
        if (!employeeOpt.get().getUserId().equals(event.getAuthorId())) {
            notificationService.createNotification(
                    employeeOpt.get().getUserId(),
                    NotificationType.TASK_COMMENT_ADDED,
                    "New Comment on Task",
                    "A new comment was added to task: \"" + task.getTitle() + "\"",
                    "TASK",
                    event.getTaskId());
        }

        // ─── Admin notification (in-app) ─────────────────────────────────
        for (UUID adminUserId : getAdminUserIds()) {
            // Skip if admin is the comment author (avoid self-notification)
            if (adminUserId.equals(event.getAuthorId()))
                continue;

            notificationService.createNotification(
                    adminUserId,
                    NotificationType.TASK_COMMENT_ADDED,
                    "New Comment on Task",
                    "A new comment was added to task: \"" + task.getTitle() + "\"",
                    "TASK",
                    event.getTaskId());
        }
    }

    // ─── Task Overdue ────────────────────────────────────────────────────────

    @EventListener
    public void handleTaskOverdue(TaskOverdueEvent event) {
        log.info("Handling TaskOverdueEvent for task [{}], {} days overdue",
                event.getTaskId(), event.getOverdueDays());

        // Resolve employee details
        Optional<Employee> employeeOpt = employeeRepository.findByIdAndIsDeletedFalse(event.getAssignedTo());
        if (employeeOpt.isEmpty()) {
            log.warn("Employee [{}] not found, skipping notification", event.getAssignedTo());
            return;
        }
        Employee employee = employeeOpt.get();
        String employeeName = employee.getFirstName() + " " + employee.getLastName();

        // Create in-app notification for employee
        notificationService.createNotification(
                employee.getUserId(),
                NotificationType.TASK_OVERDUE,
                "Task Overdue",
                "Task \"" + event.getTaskTitle() + "\" is " + event.getOverdueDays() + " day(s) overdue",
                "TASK",
                event.getTaskId());

        // Queue task-overdue email for employee
        emailQueueService.queueEmail(
                employee.getEmail(),
                "Task Overdue Alert - TaskHive",
                "task-overdue",
                Map.of(
                        "employeeName", employeeName,
                        "taskTitle", event.getTaskTitle(),
                        "dueDate", event.getDueDate().toLocalDate().toString(),
                        "overdueDays", String.valueOf(event.getOverdueDays())));

        // ─── Admin notifications (in-app + email) ────────────────────────
        for (UUID adminUserId : getAdminUserIds()) {
            // In-app notification
            notificationService.createNotification(
                    adminUserId,
                    NotificationType.TASK_OVERDUE,
                    "Task Overdue",
                    "Task \"" + event.getTaskTitle() + "\" assigned to " + employeeName +
                            " is " + event.getOverdueDays() + " day(s) overdue",
                    "TASK",
                    event.getTaskId());

            // Queue overdue email to admin
            Optional<User> adminUser = userRepository.findByIdAndIsDeletedFalse(adminUserId);
            adminUser.ifPresent(admin -> emailQueueService.queueEmail(
                    admin.getEmail(),
                    "Task Overdue Alert - TaskHive",
                    "task-overdue",
                    Map.of(
                            "recipientName", admin.getFirstName() + " " + admin.getLastName(),
                            "assigneeName", employeeName,
                            "taskTitle", event.getTaskTitle(),
                            "dueDate", event.getDueDate().toLocalDate().toString(),
                            "overdueDays", String.valueOf(event.getOverdueDays()))));
        }
    }
}

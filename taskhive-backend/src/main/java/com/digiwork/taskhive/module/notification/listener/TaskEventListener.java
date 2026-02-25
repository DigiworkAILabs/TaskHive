package com.digiwork.taskhive.module.notification.listener;

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

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final NotificationService notificationService;
    private final EmailQueueService emailQueueService;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;

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

        // Create in-app notification
        notificationService.createNotification(
                employee.getUserId(),
                NotificationType.TASK_ASSIGNED,
                "New Task Assigned",
                "You have been assigned a new task: " + event.getTaskTitle(),
                "TASK",
                event.getTaskId());

        // Queue task-assigned email
        // Resolve task details for email template
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

        // Resolve the assigned employee's userId
        Optional<Employee> employeeOpt = employeeRepository.findByIdAndIsDeletedFalse(task.getAssignedTo());
        if (employeeOpt.isEmpty()) {
            log.warn("Assigned employee [{}] not found, skipping notification", task.getAssignedTo());
            return;
        }

        notificationService.createNotification(
                employeeOpt.get().getUserId(),
                NotificationType.TASK_STATUS_CHANGED,
                "Task Status Updated",
                "Task \"" + task.getTitle() + "\" status changed from " +
                        event.getOldStatus() + " to " + event.getNewStatus(),
                "TASK",
                event.getTaskId());
    }

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

        // Don't notify the comment author themselves
        if (employeeOpt.get().getUserId().equals(event.getAuthorId())) {
            log.debug("Skipping notification — comment author is the assignee");
            return;
        }

        notificationService.createNotification(
                employeeOpt.get().getUserId(),
                NotificationType.TASK_COMMENT_ADDED,
                "New Comment on Task",
                "A new comment was added to task: \"" + task.getTitle() + "\"",
                "TASK",
                event.getTaskId());
    }

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

        // Create in-app notification
        notificationService.createNotification(
                employee.getUserId(),
                NotificationType.TASK_OVERDUE,
                "Task Overdue",
                "Task \"" + event.getTaskTitle() + "\" is " + event.getOverdueDays() + " day(s) overdue",
                "TASK",
                event.getTaskId());

        // Queue task-overdue email
        emailQueueService.queueEmail(
                employee.getEmail(),
                "Task Overdue Alert - TaskHive",
                "task-overdue",
                Map.of(
                        "employeeName", employeeName,
                        "taskTitle", event.getTaskTitle(),
                        "dueDate", event.getDueDate().toLocalDate().toString(),
                        "overdueDays", String.valueOf(event.getOverdueDays())));
    }
}

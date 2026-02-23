package com.digiwork.taskhive.module.task.scheduler;

import com.digiwork.taskhive.module.task.event.TaskOverdueEvent;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOverdueScheduler {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Runs daily at 2:00 AM to detect overdue tasks.
     * Escalation levels:
     * - 1 day overdue → WARNING (Notify Employee)
     * - 3 days overdue → ADMIN_NOTIFY (Notify Admin)
     * - 7+ days overdue → ADMIN_ESCALATION (Escalate to Admin with high priority
     * flag)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void detectOverdueTasks() {
        log.info("Starting overdue task detection...");
        LocalDateTime now = LocalDateTime.now();

        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);

        for (Task task : overdueTasks) {
            long overdueDays = ChronoUnit.DAYS.between(task.getDueDate(), now);

            String escalationLevel;
            if (overdueDays >= 7) {
                escalationLevel = "ADMIN_ESCALATION";
            } else if (overdueDays >= 3) {
                escalationLevel = "ADMIN_NOTIFY";
            } else {
                escalationLevel = "WARNING";
            }

            eventPublisher.publishEvent(new TaskOverdueEvent(
                    this,
                    task.getId(),
                    task.getAssignedTo(),
                    task.getTitle(),
                    task.getDueDate(),
                    overdueDays,
                    escalationLevel));

            log.info("Overdue task detected: {} ({}), {} days overdue, level: {}",
                    task.getTitle(), task.getId(), overdueDays, escalationLevel);
        }

        log.info("Overdue task detection complete. Found {} overdue tasks.", overdueTasks.size());
    }
}

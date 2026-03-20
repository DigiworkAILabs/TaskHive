package com.digiwork.taskhive.module.task.scheduler;

import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * P1.4 — Safety-net scheduler that runs every hour to flag tasks that have
 * become late without being submitted. Handles cases where the real-time
 * handleSubmission() path was not triggered (e.g. tasks stuck in IN_PROGRESS).
 *
 * is_late is IMMUTABLE — once set it is never cleared.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LateSubmissionScheduler {

    private final TaskRepository taskRepository;

    @Scheduled(cron = "0 0 * * * *") // every hour on the hour
    public void flagLateSubmissions() {
        log.info("LateSubmissionScheduler: scanning for un-flagged late tasks...");
        LocalDateTime now = LocalDateTime.now();

        List<Task> tasksToFlag = taskRepository.findLateInProgressTasks(now);

        int flagged = 0;
        for (Task task : tasksToFlag) {
            // Guard: immutable — skip if already flagged (should not happen due to query filter)
            if (Boolean.TRUE.equals(task.getIsLate())) {
                continue;
            }
            long minutes = ChronoUnit.MINUTES.between(task.getDueDate(), now);
            task.setIsLate(true);
            task.setLateByMinutes((int) minutes);
            taskRepository.save(task);
            flagged++;
        }

        log.info("LateSubmissionScheduler: flagged {} late tasks (scanned {} candidates)",
                flagged, tasksToFlag.size());
    }
}

package com.digiwork.taskhive.module.task.scheduler;

import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LateSubmissionScheduler (P1.4 — safety net).
 * Verifies that the scheduler does not re-flag already-late tasks.
 */
@ExtendWith(MockitoExtension.class)
class LateSubmissionSchedulerTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private LateSubmissionScheduler scheduler;

    @Test
    @DisplayName("scheduler should not throw even if no tasks qualify")
    void noExceptionWhenNoLateTasks() {
        when(taskRepository.findLateInProgressTasks(any())).thenReturn(List.of());

        assertThatNoException().isThrownBy(scheduler::flagLateSubmissions);
    }

    @Test
    @DisplayName("scheduler flags tasks that are past due date and not already late")
    void flagsTasksPastDueDate() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .dueDate(LocalDateTime.now().minusHours(3))
                .isLate(false)
                .build();

        when(taskRepository.findLateInProgressTasks(any())).thenReturn(List.of(task));

        scheduler.flagLateSubmissions();

        verify(taskRepository).save(task);
        org.assertj.core.api.Assertions.assertThat(task.getIsLate()).isTrue();
    }

    @Test
    @DisplayName("scheduler does NOT re-flag tasks that are already marked late")
    void doesNotReFlagAlreadyLateTask() {
        Task alreadyLate = Task.builder()
                .id(UUID.randomUUID())
                .dueDate(LocalDateTime.now().minusHours(5))
                .isLate(true)
                .lateByMinutes(300)
                .build();

        // Simulate a broken query that returns an already-late task
        when(taskRepository.findLateInProgressTasks(any())).thenReturn(List.of(alreadyLate));

        scheduler.flagLateSubmissions();

        // Even if returned by the query, the scheduler should not overwrite existing late data
        assertThat(alreadyLate.getIsLate()).isTrue();
        assertThat(alreadyLate.getLateByMinutes()).isEqualTo(300);
    }
}

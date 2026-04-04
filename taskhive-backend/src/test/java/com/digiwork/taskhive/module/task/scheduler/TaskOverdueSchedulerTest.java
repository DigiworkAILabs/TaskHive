package com.digiwork.taskhive.module.task.scheduler;

import com.digiwork.taskhive.module.task.event.TaskOverdueEvent;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskOverdueSchedulerTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskOverdueScheduler taskOverdueScheduler;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Overdue Task")
                .assignedTo(UUID.randomUUID())
                .dueDate(LocalDateTime.now().minusDays(1)) // 1 day overdue
                .build();
    }

    @Test
    @DisplayName("detectOverdueTasks: should publish WARNING event for 1 day overdue")
    void detectOverdueTasks_Warning() {
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(sampleTask));

        taskOverdueScheduler.detectOverdueTasks();

        verify(eventPublisher).publishEvent(argThat(event -> 
            event instanceof TaskOverdueEvent && 
            ((TaskOverdueEvent) event).getEscalationLevel().equals("WARNING")
        ));
    }

    @Test
    @DisplayName("detectOverdueTasks: should publish ADMIN_NOTIFY event for 3 days overdue")
    void detectOverdueTasks_AdminNotify() {
        sampleTask.setDueDate(LocalDateTime.now().minusDays(4)); // 4 days overdue
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(sampleTask));

        taskOverdueScheduler.detectOverdueTasks();

        verify(eventPublisher).publishEvent(argThat(event -> 
            event instanceof TaskOverdueEvent && 
            ((TaskOverdueEvent) event).getEscalationLevel().equals("ADMIN_NOTIFY")
        ));
    }

    @Test
    @DisplayName("detectOverdueTasks: should publish ADMIN_ESCALATION event for 7+ days overdue")
    void detectOverdueTasks_AdminEscalation() {
        sampleTask.setDueDate(LocalDateTime.now().minusDays(8)); // 8 days overdue
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(sampleTask));

        taskOverdueScheduler.detectOverdueTasks();

        verify(eventPublisher).publishEvent(argThat(event -> 
            event instanceof TaskOverdueEvent && 
            ((TaskOverdueEvent) event).getEscalationLevel().equals("ADMIN_ESCALATION")
        ));
    }

    @Test
    @DisplayName("detectOverdueTasks: should do nothing when no overdue tasks")
    void detectOverdueTasks_Empty() {
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of());
        
        taskOverdueScheduler.detectOverdueTasks();
        
        verifyNoInteractions(eventPublisher);
    }
}

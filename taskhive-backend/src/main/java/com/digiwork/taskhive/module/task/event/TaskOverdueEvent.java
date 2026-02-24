package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TaskOverdueEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID assignedTo;
    private final String taskTitle;
    private final LocalDateTime dueDate;
    private final long overdueDays;
    private final String escalationLevel; // WARNING, ADMIN_NOTIFY, ADMIN_ESCALATION

    public TaskOverdueEvent(Object source, UUID taskId, UUID assignedTo, String taskTitle,
            LocalDateTime dueDate, long overdueDays, String escalationLevel) {
        super(source);
        this.taskId = taskId;
        this.assignedTo = assignedTo;
        this.taskTitle = taskTitle;
        this.dueDate = dueDate;
        this.overdueDays = overdueDays;
        this.escalationLevel = escalationLevel;
    }
}

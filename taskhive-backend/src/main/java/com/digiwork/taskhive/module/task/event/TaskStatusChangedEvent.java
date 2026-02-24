package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskStatusChangedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final String oldStatus;
    private final String newStatus;
    private final UUID changedBy;

    public TaskStatusChangedEvent(Object source, UUID taskId, String oldStatus, String newStatus, UUID changedBy) {
        super(source);
        this.taskId = taskId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
    }
}

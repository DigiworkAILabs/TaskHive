package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskCreatedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID createdBy;

    public TaskCreatedEvent(Object source, UUID taskId, UUID createdBy) {
        super(source);
        this.taskId = taskId;
        this.createdBy = createdBy;
    }
}

package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskUpdatedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID updatedBy;

    public TaskUpdatedEvent(Object source, UUID taskId, UUID updatedBy) {
        super(source);
        this.taskId = taskId;
        this.updatedBy = updatedBy;
    }
}

package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskAssignedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID employeeId;
    private final String taskTitle;
    private final UUID assignedBy;

    public TaskAssignedEvent(Object source, UUID taskId, UUID employeeId, String taskTitle, UUID assignedBy) {
        super(source);
        this.taskId = taskId;
        this.employeeId = employeeId;
        this.taskTitle = taskTitle;
        this.assignedBy = assignedBy;
    }
}

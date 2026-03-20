package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskApprovedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID assigneeEmployeeId;
    private final UUID approvedByUserId;
    private final String taskTitle;

    public TaskApprovedEvent(Object source, UUID taskId, UUID assigneeEmployeeId,
                             UUID approvedByUserId, String taskTitle) {
        super(source);
        this.taskId = taskId;
        this.assigneeEmployeeId = assigneeEmployeeId;
        this.approvedByUserId = approvedByUserId;
        this.taskTitle = taskTitle;
    }
}

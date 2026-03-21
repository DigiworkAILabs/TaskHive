package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskRejectedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID assigneeEmployeeId;
    private final UUID rejectedByUserId;
    private final String taskTitle;
    private final String rejectionReason;

    public TaskRejectedEvent(Object source, UUID taskId, UUID assigneeEmployeeId,
                             UUID rejectedByUserId, String taskTitle, String rejectionReason) {
        super(source);
        this.taskId = taskId;
        this.assigneeEmployeeId = assigneeEmployeeId;
        this.rejectedByUserId = rejectedByUserId;
        this.taskTitle = taskTitle;
        this.rejectionReason = rejectionReason;
    }
}

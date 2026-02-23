package com.digiwork.taskhive.module.task.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class TaskCommentAddedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID commentId;
    private final UUID authorId;

    public TaskCommentAddedEvent(Object source, UUID taskId, UUID commentId, UUID authorId) {
        super(source);
        this.taskId = taskId;
        this.commentId = commentId;
        this.authorId = authorId;
    }
}

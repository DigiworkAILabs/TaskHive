package com.digiwork.taskhive.module.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentResponse {

    private String id;
    private String taskId;
    private String authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}

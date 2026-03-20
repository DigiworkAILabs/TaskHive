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
public class TaskAttachmentResponse {

    private String id;
    private String taskId;
    private String uploadedBy;
    private String uploaderName;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private String attachmentPurpose;   // P1.1 — "PROOF" or "GENERAL"
    private LocalDateTime createdAt;
}

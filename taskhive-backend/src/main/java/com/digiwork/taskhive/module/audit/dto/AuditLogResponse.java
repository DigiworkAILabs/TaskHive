package com.digiwork.taskhive.module.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;
    private UUID actorId;
    private String actorEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String beforeState;
    private String afterState;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}

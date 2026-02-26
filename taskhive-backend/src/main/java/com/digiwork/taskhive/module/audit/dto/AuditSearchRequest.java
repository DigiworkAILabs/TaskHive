package com.digiwork.taskhive.module.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSearchRequest {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String action;
    private String entityType;
    private String actorEmail;
    private String ipAddress;
}

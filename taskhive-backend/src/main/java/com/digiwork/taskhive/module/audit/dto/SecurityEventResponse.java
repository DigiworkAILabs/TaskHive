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
public class SecurityEventResponse {

    private UUID id;
    private String eventType;
    private UUID userId;
    private String ipAddress;
    private boolean success;
    private String details;
    private LocalDateTime timestamp;
}

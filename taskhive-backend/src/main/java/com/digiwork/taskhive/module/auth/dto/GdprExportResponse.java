package com.digiwork.taskhive.module.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * GDPR data export response containing all personal data for a user
 * (NFR-SEC-13).
 */
public record GdprExportResponse(
        UserData user,
        EmployeeData employee,
        List<TaskSummaryData> tasksCreated,
        List<TaskSummaryData> tasksAssigned,
        List<CommentData> comments,
        long notificationCount,
        long auditLogCount,
        LocalDateTime exportedAt) {

    public record UserData(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String role,
            String status,
            LocalDateTime createdAt) {
    }

    public record EmployeeData(
            String department,
            String designation,
            String phone,
            LocalDate joinDate) {
    }

    public record TaskSummaryData(
            UUID id,
            String title,
            String status,
            String priority,
            LocalDateTime dueDate,
            LocalDateTime createdAt) {
    }

    public record CommentData(
            UUID taskId,
            String content,
            LocalDateTime createdAt) {
    }
}

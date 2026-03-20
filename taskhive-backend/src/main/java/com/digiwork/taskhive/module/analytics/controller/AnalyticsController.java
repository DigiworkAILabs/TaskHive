package com.digiwork.taskhive.module.analytics.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.analytics.dto.*;
import com.digiwork.taskhive.module.analytics.model.FileMetadata;
import com.digiwork.taskhive.module.analytics.service.AnalyticsService;
import com.digiwork.taskhive.module.analytics.service.AnomalyDetectionService;
import com.digiwork.taskhive.module.analytics.service.ReportService;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportService reportService;
    private final AnomalyDetectionService anomalyDetectionService;

    // ─── GET /api/v1/analytics/dashboard/admin ───────────────────────────────

    @GetMapping("/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse dashboard = analyticsService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard retrieved successfully", dashboard));
    }

    // ─── GET /api/v1/analytics/dashboard/employee ────────────────────────────

    @GetMapping("/dashboard/employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeDashboard() {
        UUID userId = SecurityUtils.getCurrentUserId();
        EmployeeDashboardResponse dashboard = analyticsService.getEmployeeDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success("Employee dashboard retrieved successfully", dashboard));
    }

    // ─── GET /api/v1/analytics/tasks/distribution ────────────────────────────

    @GetMapping("/tasks/distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskDistributionResponse>>> getTaskDistribution() {
        List<TaskDistributionResponse> distribution = analyticsService.getTaskDistribution();
        return ResponseEntity.ok(ApiResponse.success("Task distribution retrieved successfully", distribution));
    }

    // ─── GET /api/v1/analytics/tasks/by-priority ─────────────────────────────

    @GetMapping("/tasks/by-priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskDistributionResponse>>> getTaskByPriority() {
        List<TaskDistributionResponse> distribution = analyticsService.getTaskByPriority();
        return ResponseEntity
                .ok(ApiResponse.success("Task priority distribution retrieved successfully", distribution));
    }

    // ─── GET /api/v1/analytics/tasks/completion-trend ────────────────────────

    @GetMapping("/tasks/completion-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TaskCompletionTrendResponse>>> getCompletionTrend() {
        List<TaskCompletionTrendResponse> trend = analyticsService.getCompletionTrend();
        return ResponseEntity.ok(ApiResponse.success("Completion trend retrieved successfully", trend));
    }

    // ─── GET /api/v1/analytics/employees/performance ─────────────────────────

    @GetMapping("/employees/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeePerformanceResponse>>> getEmployeePerformance() {
        List<EmployeePerformanceResponse> performance = analyticsService.getEmployeePerformance();
        return ResponseEntity.ok(ApiResponse.success("Employee performance retrieved successfully", performance));
    }

    // ─── POST /api/v1/analytics/reports/export ───────────────────────────────

    @PostMapping("/reports/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportReport(
            @RequestBody ReportExportRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID reportId = reportService.exportReport(request, userId);

        Map<String, String> response = Map.of(
                "reportId", reportId.toString(),
                "message", "Report generation started. Use the download endpoint when ready.",
                "downloadUrl", "/api/v1/analytics/reports/" + reportId + "/download");

        return ResponseEntity.ok()
                .body(ApiResponse.success("Report exported successfully", response));
    }

    // ─── GET /api/v1/analytics/reports/{id}/download ─────────────────────────

    @GetMapping("/reports/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadReport(@PathVariable UUID id) {
        Resource resource = reportService.downloadReport(id);
        FileMetadata metadata = reportService.getReportMetadata(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getFileName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    // ─── P1.5: GET /api/v1/analytics/dashboard/today ─────────────────────────

    @GetMapping("/dashboard/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TodayOverviewResponse>> getTodayOverview() {
        int activeAnomalyCount = anomalyDetectionService.getActiveAlerts().size();
        TodayOverviewResponse overview = analyticsService.getTodayOverview(activeAnomalyCount);
        return ResponseEntity.ok(ApiResponse.success("Today's overview retrieved successfully", overview));
    }

    // ─── P1.5: GET /api/v1/analytics/anomalies ───────────────────────────────

    @GetMapping("/anomalies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnomalyAlertResponse>>> getAnomalies() {
        return ResponseEntity.ok(ApiResponse.success("Active anomaly alerts retrieved",
                anomalyDetectionService.getActiveAlerts()));
    }

    // ─── P1.5: GET /api/v1/analytics/tasks/missed ────────────────────────────

    @GetMapping("/tasks/missed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<com.digiwork.taskhive.module.task.dto.TaskListResponse>>> getMissedTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Missed tasks retrieved",
                analyticsService.getMissedTasks(pageable)));
    }

    // ─── P1.5: GET /api/v1/analytics/tasks/late ──────────────────────────────

    @GetMapping("/tasks/late")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<com.digiwork.taskhive.module.task.dto.TaskListResponse>>> getLatePatterns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Late submission patterns retrieved",
                analyticsService.getLatePatterns(pageable)));
    }
}

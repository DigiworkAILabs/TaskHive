package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.dto.ReportExportRequest;
import com.digiwork.taskhive.module.analytics.model.FileMetadata;
import com.digiwork.taskhive.module.analytics.repository.AnalyticsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AnalyticsRepository analyticsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${storage.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${storage.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    // ─── Report Export ───────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressWarnings("unchecked")
    public UUID exportReport(ReportExportRequest request, UUID userId) {
        log.info("Starting report export: type={}, userId={}", request.getReportType(), userId);

        try {
            String csvContent;
            String reportType = request.getReportType().toUpperCase();

            csvContent = switch (reportType) {
                case "TASK_SUMMARY" -> generateTaskSummaryCsv();
                case "EMPLOYEE_PERFORMANCE" -> generateEmployeePerformanceCsv();
                default -> throw new IllegalArgumentException(
                        "Invalid report type: " + reportType +
                                ". Valid types: TASK_SUMMARY, EMPLOYEE_PERFORMANCE");
            };

            // Save CSV file to disk
            String fileName = reportType.toLowerCase() + "_report_" +
                    System.currentTimeMillis() + ".csv";
            Path reportDir = Paths.get(uploadDir, "reports");
            Files.createDirectories(reportDir);

            Path filePath = reportDir.resolve(fileName);
            Files.writeString(filePath, csvContent);

            long fileSize = Files.size(filePath);
            String fileUrl = "reports/" + fileName;

            // Store metadata
            FileMetadata metadata = FileMetadata.builder()
                    .entityType("REPORT")
                    .entityId(null)
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .fileSize(fileSize)
                    .mimeType("text/csv")
                    .storageType("LOCAL")
                    .uploadedBy(userId)
                    .build();

            FileMetadata saved = analyticsRepository.save(metadata);
            log.info("Report exported: id={}, file={}, size={} bytes", saved.getId(), fileName, fileSize);

            return saved.getId();

        } catch (IOException e) {
            log.error("Failed to export report: type={}", request.getReportType(), e);
            throw new RuntimeException("Failed to generate report file", e);
        }
    }

    // ─── Download Report ─────────────────────────────────────────────────────

    public Resource downloadReport(UUID fileId) {
        FileMetadata metadata = analyticsRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + fileId));

        // Validate 24-hour expiry
        if (metadata.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Report download link has expired (24-hour retention)");
        }

        Path filePath = Paths.get(uploadDir, metadata.getFileUrl());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Report file not found on disk: " + metadata.getFileName());
        }

        return new FileSystemResource(filePath);
    }

    public FileMetadata getReportMetadata(UUID fileId) {
        return analyticsRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + fileId));
    }

    // ─── CSV Generation (StringBuilder pattern from ComplianceReportService) ─

    @SuppressWarnings("unchecked")
    private String generateTaskSummaryCsv() {
        String sql = "SELECT t.id, t.title, t.status, t.priority, t.assigned_to, " +
                "t.due_date, t.completed_at, t.created_at " +
                "FROM tasks t WHERE t.is_deleted = false ORDER BY t.created_at DESC";

        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

        StringBuilder sb = new StringBuilder();
        sb.append("Task ID,Title,Status,Priority,Assigned To,Due Date,Completed At,Created At\n");

        for (Object[] row : rows) {
            sb.append(csvValue(row[0])).append(',')
                    .append(csvValue(row[1])).append(',')
                    .append(csvValue(row[2])).append(',')
                    .append(csvValue(row[3])).append(',')
                    .append(csvValue(row[4])).append(',')
                    .append(csvValue(row[5])).append(',')
                    .append(csvValue(row[6])).append(',')
                    .append(csvValue(row[7])).append('\n');
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String generateEmployeePerformanceCsv() {
        String sql = "SELECT t.assigned_to, " +
                "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, " +
                "COUNT(*) AS tasks_assigned, " +
                "COUNT(*) FILTER (WHERE t.status = 'DONE') AS tasks_completed, " +
                "COALESCE(COUNT(*) FILTER (WHERE t.status = 'DONE' AND t.completed_at <= t.due_date), 0) AS on_time, " +
                "COALESCE(AVG(EXTRACT(EPOCH FROM (t.completed_at - t.created_at)) / 3600) " +
                "FILTER (WHERE t.status = 'DONE' AND t.completed_at IS NOT NULL), 0) AS avg_hours " +
                "FROM tasks t " +
                "LEFT JOIN employees e ON e.id = t.assigned_to " +
                "WHERE t.is_deleted = false " +
                "GROUP BY t.assigned_to, e.first_name, e.last_name " +
                "ORDER BY tasks_completed DESC";

        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

        StringBuilder sb = new StringBuilder();
        sb.append("Employee ID,Employee Name,Tasks Assigned,Tasks Completed,On-Time Completed,Avg Completion Hours\n");

        for (Object[] row : rows) {
            sb.append(csvValue(row[0])).append(',')
                    .append(csvValue(row[1])).append(',')
                    .append(csvValue(row[2])).append(',')
                    .append(csvValue(row[3])).append(',')
                    .append(csvValue(row[4])).append(',')
                    .append(csvValue(row[5])).append('\n');
        }

        return sb.toString();
    }

    private String csvValue(Object value) {
        if (value == null)
            return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}

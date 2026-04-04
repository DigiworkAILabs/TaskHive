package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.dto.ReportExportRequest;
import com.digiwork.taskhive.module.analytics.model.FileMetadata;
import com.digiwork.taskhive.module.analytics.repository.AnalyticsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ReportService reportService;

    @TempDir
    Path tempDir;

    private UUID userId;
    private UUID fileId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fileId = UUID.randomUUID();
        ReflectionTestUtils.setField(reportService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(reportService, "entityManager", entityManager);
    }

    @Test
    @DisplayName("exportReport: should generate task summary CSV and save metadata")
    void exportReport_TaskSummary() {
        ReportExportRequest request = new ReportExportRequest();
        request.setReportType("TASK_SUMMARY");

        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(new Object[]{"ID", "Title", "TODO", "HIGH", "Assignee", "2026-03-28", null, "2026-03-27"});
        when(mockQuery.getResultList()).thenReturn(rows);

        when(analyticsRepository.save(any(FileMetadata.class))).thenAnswer(invocation -> {
            FileMetadata fm = invocation.getArgument(0);
            fm.setId(fileId);
            return fm;
        });

        UUID resultId = reportService.exportReport(request, userId);

        assertThat(resultId).isEqualTo(fileId);
        verify(analyticsRepository).save(argThat(fm -> 
            fm.getEntityType().equals("REPORT") && 
            fm.getFileName().startsWith("task_summary_report_")
        ));
    }

    @Test
    @DisplayName("exportReport: should throw exception for invalid report type")
    void exportReport_InvalidType() {
        ReportExportRequest request = new ReportExportRequest();
        request.setReportType("INVALID");

        assertThatThrownBy(() -> reportService.exportReport(request, userId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("downloadReport: should return resource for valid file")
    void downloadReport_Success() throws IOException {
        Path reportPath = tempDir.resolve("reports/test.csv");
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, "data");

        FileMetadata metadata = FileMetadata.builder()
                .fileUrl("reports/test.csv")
                .createdAt(LocalDateTime.now())
                .build();
        when(analyticsRepository.findById(fileId)).thenReturn(Optional.of(metadata));

        Resource resource = reportService.downloadReport(fileId);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    @DisplayName("downloadReport: should throw if link expired")
    void downloadReport_Expired() {
        FileMetadata metadata = FileMetadata.builder()
                .createdAt(LocalDateTime.now().minusHours(25))
                .build();
        when(analyticsRepository.findById(fileId)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> reportService.downloadReport(fileId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("downloadReport: should throw if file not found on disk")
    void downloadReport_FileNotFoundOnDisk() {
        FileMetadata metadata = FileMetadata.builder()
                .fileUrl("nonexistent.csv")
                .createdAt(LocalDateTime.now())
                .build();
        when(analyticsRepository.findById(fileId)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> reportService.downloadReport(fileId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found on disk");
    }
}

package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import com.digiwork.taskhive.module.analytics.repository.EmployeePerformanceCacheRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsAggregatorServiceTest {

    @Mock
    private DailyMetricsRepository dailyMetricsRepository;
    @Mock
    private EmployeePerformanceCacheRepository employeePerformanceCacheRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private MetricsAggregatorService metricsAggregatorService;

    @BeforeEach
    void setUp() {
        // Inject the persistence context mock manually
        ReflectionTestUtils.setField(metricsAggregatorService, "entityManager", entityManager);
    }

    @Test
    @DisplayName("calculateDailyMetrics: should correctly calculate and upsert all 6 metric dimensions")
    void calculateDailyMetrics_Success() {
        LocalDate date = LocalDate.now();
        Query query = mock(Query.class);
        
        // Setup sequential mocks for 6 queries: 
        // 1. Total tasks (L44)
        // 2. Active tasks (L50)
        // 3. Overdue tasks (L57)
        // 4. Completed tasks (L62) 
        // 5. Avg completion hours (L78)
        // 6. Total employees (L87)
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        
        when(query.getSingleResult())
            .thenReturn(100)  // totalTasks
            .thenReturn(60)   // activeTasks
            .thenReturn(10)   // overdueTasks
            .thenReturn(40)   // completedTasks
            .thenReturn(12.5) // avgCompletionHours
            .thenReturn(25);  // totalEmployees

        DailyMetrics result = metricsAggregatorService.calculateDailyMetrics(date);

        // Assertions
        assertThat(result.getTotalTasks()).isEqualTo(100);
        assertThat(result.getActiveTasks()).isEqualTo(60);
        assertThat(result.getOverdueTasks()).isEqualTo(10);
        assertThat(result.getCompletedTasks()).isEqualTo(40);
        assertThat(result.getCompletionRate()).isEqualByComparingTo("40.00");
        assertThat(result.getAvgCompletionHours()).isEqualByComparingTo("12.50");
        assertThat(result.getTotalEmployees()).isEqualTo(25);

        // Verify repository interaction
        verify(dailyMetricsRepository).upsertDailyMetrics(argThat(m -> 
            m.getMetricDate().equals(date) &&
            m.getTotalTasks() == 100 &&
            m.getCompletionRate().compareTo(new BigDecimal("40.00")) == 0
        ));
    }

    @Test
    @DisplayName("calculateDailyMetrics: should handle zero tasks case")
    void calculateDailyMetrics_ZeroTasks() {
        LocalDate date = LocalDate.now();
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        // 6 queries returning zero or equivalent
        when(query.getSingleResult())
            .thenReturn(0)   // totalTasks
            .thenReturn(0)   // activeTasks
            .thenReturn(0)   // overdueTasks
            .thenReturn(0)   // completedTasks
            .thenReturn(0.0) // avgCompletionHours
            .thenReturn(5);  // totalEmployees

        DailyMetrics result = metricsAggregatorService.calculateDailyMetrics(date);

        assertThat(result.getCompletionRate()).isEqualByComparingTo("0.00");
        assertThat(result.getAvgCompletionHours()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("calculateEmployeePerformance: should aggregate rows and upsert cache")
    void calculateEmployeePerformance_Success() {
        LocalDate date = LocalDate.now();
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        // Mock result list for performance rows
        UUID empId1 = UUID.randomUUID();
        UUID empId2 = UUID.randomUUID();
        List<Object[]> rows = new ArrayList<>();
        // Row: [employeeId, assigned, completed, onTime, avgHours]
        rows.add(new Object[]{empId1, 10, 8, 6L, 15.2});
        rows.add(new Object[]{empId2, 5, 2, 1L, 20.0});
        
        when(query.getResultList()).thenReturn(rows);

        int processedCount = metricsAggregatorService.calculateEmployeePerformance(date);

        assertThat(processedCount).isEqualTo(2);

        // Verify upsert for employee 1
        verify(employeePerformanceCacheRepository).upsertEmployeePerformance(
            eq(empId1), any(LocalDate.class), any(LocalDate.class),
            eq(10), eq(8), eq(new BigDecimal("75.00")), eq(new BigDecimal("15.20"))
        );

        // Verify upsert for employee 2
        verify(employeePerformanceCacheRepository).upsertEmployeePerformance(
            eq(empId2), any(LocalDate.class), any(LocalDate.class),
            eq(5), eq(2), eq(new BigDecimal("50.00")), eq(new BigDecimal("20.00"))
        );
    }
}

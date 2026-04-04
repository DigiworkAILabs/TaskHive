package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.ml.dto.*;
import com.digiwork.taskhive.module.ml.model.MLPredictionLog;
import com.digiwork.taskhive.module.ml.repository.MLPredictionLogRepository;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskCommentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MLServiceTest {

    @Mock
    private MLClientService mlClientService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TaskCommentRepository taskCommentRepository;
    @Mock
    private MLPredictionLogRepository predictionLogRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MLService mlService;

    private UUID employeeId;
    private Task dummyTask;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        dummyTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .status("DONE")
                .estimatedHours(java.math.BigDecimal.valueOf(4.0))
                .createdAt(LocalDateTime.now().minusDays(5))
                .completedAt(LocalDateTime.now().minusDays(1))
                .dueDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("predictPriority should return valid response")
    void predictPriority() {
        TaskPriorityRequest req = new TaskPriorityRequest();
        req.setTaskTitle("Test");
        req.setEmployeeId(employeeId);

        TaskPriorityResponse mockResponse = new TaskPriorityResponse();
        mockResponse.setPredictedPriority("HIGH");

        when(taskRepository.findByAssignedToAndIsDeletedFalse(employeeId)).thenReturn(List.of(dummyTask));
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.of(Employee.builder().department("IT").build()));
        when(mlClientService.predictPriority(any())).thenReturn(mockResponse);

        TaskPriorityResponse res = mlService.predictPriority(req);

        assertThat(res.getPredictedPriority()).isEqualTo("HIGH");
        verify(mlClientService).predictPriority(any());
    }

    @Test
    @DisplayName("predictCompletionTime should return valid response")
    void predictCompletionTime() {
        CompletionTimeRequest req = new CompletionTimeRequest();
        req.setTaskTitle("Test");
        req.setEmployeeId(employeeId);

        CompletionTimeResponse mockResponse = new CompletionTimeResponse();
        mockResponse.setEstimatedHours(5.5);

        when(taskRepository.findByAssignedToAndIsDeletedFalse(employeeId)).thenReturn(List.of(dummyTask));
        when(mlClientService.predictCompletionTime(any())).thenReturn(mockResponse);

        CompletionTimeResponse res = mlService.predictCompletionTime(req);

        assertThat(res.getEstimatedHours()).isEqualTo(5.5);
        verify(mlClientService).predictCompletionTime(any());
    }

    @Test
    @DisplayName("recommendWorkloadBalance should return valid response")
    void recommendWorkloadBalance() {
        WorkloadRecommendationRequest req = new WorkloadRecommendationRequest();
        req.setCandidateEmployeeIds(List.of(employeeId.toString()));
        req.setTaskEstimatedHours(5.0);

        WorkloadRecommendationResponse mockResponse = new WorkloadRecommendationResponse();
        mockResponse.setRecommendedEmployeeId(employeeId.toString());

        when(taskRepository.findByAssignedToAndIsDeletedFalse(employeeId)).thenReturn(List.of(dummyTask));
        when(employeeRepository.findByIdAndIsDeletedFalse(employeeId)).thenReturn(Optional.empty());
        when(mlClientService.recommendWorkloadBalance(any())).thenReturn(mockResponse);

        WorkloadRecommendationResponse res = mlService.recommendWorkloadBalance(req);

        assertThat(res.getRecommendedEmployeeId()).isEqualTo(employeeId.toString());
        verify(mlClientService).recommendWorkloadBalance(any());
    }

    @Test
    @DisplayName("recommendWorkloadBalance should handle candidate enrichment failure")
    void recommendWorkloadBalance_CandidateFailure() {
        WorkloadRecommendationRequest req = new WorkloadRecommendationRequest();
        req.setCandidateEmployeeIds(List.of("invalid-id", employeeId.toString()));
        req.setTaskEstimatedHours(5.0);

        WorkloadRecommendationResponse mockResponse = new WorkloadRecommendationResponse();
        when(mlClientService.recommendWorkloadBalance(any())).thenReturn(mockResponse);

        WorkloadRecommendationResponse res = mlService.recommendWorkloadBalance(req);

        assertThat(res).isNotNull();
        verify(mlClientService).recommendWorkloadBalance(argThat(payload -> 
            ((List<?>)payload.get("candidates")).size() == 1 // Only the valid ID should be present
        ));
    }

    @Test
    @DisplayName("predictProductivityScore should return valid response and save log")
    void predictProductivityScore() {
        ProductivityScoreResponse mockResponse = new ProductivityScoreResponse();
        mockResponse.setScore(85.5);
        mockResponse.setFallbackUsed(false);
        mockResponse.setReasoning("Good job");

        MLPredictionLog prevLog = MLPredictionLog.builder()
                .outputData(Map.of("score", 80.0))
                .build();

        when(taskRepository.findByAssignedToAndIsDeletedFalse(employeeId)).thenReturn(List.of(dummyTask));
        when(taskCommentRepository.countByAuthorIdAndCreatedAtAfter(eq(employeeId), any())).thenReturn(5L);
        when(predictionLogRepository.findLatestByFeatureAndEmployee("PRODUCTIVITY", employeeId))
                .thenReturn(Optional.of(prevLog));
        when(mlClientService.predictProductivityScore(any())).thenReturn(mockResponse);
        when(userRepository.findById(employeeId)).thenReturn(Optional.empty());

        ProductivityScoreResponse res = mlService.predictProductivityScore(employeeId, 30);

        assertThat(res.getScore()).isEqualTo(85.5);
        verify(mlClientService).predictProductivityScore(argThat(payload -> 
            payload.get("prev_score").equals(80.0)
        ));
        verify(predictionLogRepository).save(any(MLPredictionLog.class));
    }

    @Test
    @DisplayName("predictProductivityScore should handle empty task list and default rates")
    void predictProductivityScore_EmptyTasks() {
        when(taskRepository.findByAssignedToAndIsDeletedFalse(employeeId)).thenReturn(Collections.emptyList());
        when(predictionLogRepository.findLatestByFeatureAndEmployee("PRODUCTIVITY", employeeId))
                .thenReturn(Optional.empty());
        when(mlClientService.predictProductivityScore(any())).thenReturn(new ProductivityScoreResponse());

        mlService.predictProductivityScore(employeeId, 30);

        verify(mlClientService).predictProductivityScore(argThat(payload -> 
            payload.get("on_time_rate").equals(0.75) && 
            payload.get("avg_completion_hours").equals(4.0)
        ));
    }
}

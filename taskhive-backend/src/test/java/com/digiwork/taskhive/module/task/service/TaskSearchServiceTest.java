package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.task.dto.TaskListResponse;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskSearchServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskSearchService taskSearchService;

    private Task task;
    private TaskListResponse taskListResponse;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .build();

        taskListResponse = new TaskListResponse();
        taskListResponse.setTitle("Test Task");
    }

    @Test
    @DisplayName("searchTasks: should return mapped PageResponse with filter verification")
    void searchTasks_Success() {
        String query = "Test";
        UUID assignedTo = UUID.randomUUID();
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        
        List<Task> content = List.of(task);
        Page<Task> taskPage = new PageImpl<>(content, pageable, 1);

        when(taskRepository.searchTasksExtended(eq(query), eq(assignedTo), any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toTaskListResponse(task)).thenReturn(taskListResponse);

        PageResponse<TaskListResponse> response = taskSearchService.searchTasks(query, assignedTo, page, size);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Test Task");
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();

        verify(taskRepository).searchTasksExtended(eq(query), eq(assignedTo), any(Pageable.class));
    }
}

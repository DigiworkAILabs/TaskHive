package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.task.dto.TaskListResponse;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSearchService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> searchTasks(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.searchFullText(query, pageable);

        return PageResponse.<TaskListResponse>builder()
                .content(taskPage.getContent().stream()
                        .map(taskMapper::toTaskListResponse)
                        .toList())
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }
}

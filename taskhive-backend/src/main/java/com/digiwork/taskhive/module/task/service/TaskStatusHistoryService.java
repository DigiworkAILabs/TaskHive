package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.module.task.dto.TaskStatusHistoryResponse;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import com.digiwork.taskhive.module.task.repository.TaskStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskStatusHistoryService {

    private final TaskStatusHistoryRepository historyRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryResponse> getHistory(UUID taskId) {
        // Verify task exists
        taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        return historyRepository.findByTaskIdOrderByChangedAtDesc(taskId).stream()
                .map(taskMapper::toTaskStatusHistoryResponse)
                .toList();
    }
}

package com.digiwork.taskhive.module.task.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.auth.security.SecurityUtils;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.dto.TaskCommentRequest;
import com.digiwork.taskhive.module.task.dto.TaskCommentResponse;
import com.digiwork.taskhive.module.task.event.TaskCommentAddedEvent;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import com.digiwork.taskhive.module.task.mapper.TaskMapper;
import com.digiwork.taskhive.module.task.model.Task;
import com.digiwork.taskhive.module.task.model.TaskComment;
import com.digiwork.taskhive.module.task.repository.TaskCommentRepository;
import com.digiwork.taskhive.module.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TaskCommentResponse addComment(UUID taskId, TaskCommentRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();

        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        // EMPLOYEE can only comment on their assigned tasks
        if ("EMPLOYEE".equals(currentRole)) {
            var employee = employeeRepository.findByUserIdAndIsDeletedFalse(currentUserId)
                    .orElseThrow(() -> new TaskAccessDeniedException("Employee record not found"));
            if (!task.getAssignedTo().equals(employee.getId())) {
                throw new TaskAccessDeniedException("You can only comment on tasks assigned to you");
            }
        }

        TaskComment comment = TaskComment.builder()
                .taskId(taskId)
                .authorId(currentUserId)
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);

        eventPublisher.publishEvent(new TaskCommentAddedEvent(
                this, taskId, comment.getId(), currentUserId));

        log.info("Comment added to task {}: {}", taskId, comment.getId());

        return taskMapper.toTaskCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskCommentResponse> getComments(UUID taskId, int page, int size) {
        // Verify task exists
        taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        Pageable pageable = PageRequest.of(page, size);
        Page<TaskComment> commentPage = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);

        return PageResponse.<TaskCommentResponse>builder()
                .content(commentPage.getContent().stream()
                        .map(taskMapper::toTaskCommentResponse)
                        .toList())
                .page(commentPage.getNumber())
                .size(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .last(commentPage.isLast())
                .build();
    }
}

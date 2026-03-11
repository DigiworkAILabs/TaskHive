package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.task.model.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    Page<TaskComment> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable);

    // GDPR — fetch all comments by a user (for data export)
    List<TaskComment> findByAuthorId(UUID authorId);

    long countByAuthorIdAndCreatedAtAfter(UUID authorId, java.time.LocalDateTime date);
}

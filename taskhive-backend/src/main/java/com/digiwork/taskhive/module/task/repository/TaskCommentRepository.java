package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.task.model.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    Page<TaskComment> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable);
}

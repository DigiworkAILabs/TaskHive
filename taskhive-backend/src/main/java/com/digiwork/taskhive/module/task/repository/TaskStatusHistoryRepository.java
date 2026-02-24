package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.task.model.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, UUID> {

    List<TaskStatusHistory> findByTaskIdOrderByChangedAtDesc(UUID taskId);
}

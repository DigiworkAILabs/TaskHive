package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

        Optional<Task> findByIdAndIsDeletedFalse(UUID id);

        @Query(value = "SELECT * FROM tasks t WHERE t.is_deleted = false " +
                        "AND (CAST(:status AS VARCHAR) IS NULL OR t.status = :status) " +
                        "AND (CAST(:priority AS VARCHAR) IS NULL OR t.priority = :priority) " +
                        "AND (CAST(:assignedTo AS UUID) IS NULL OR t.assigned_to = CAST(:assignedTo AS UUID)) " +
                        "AND (CAST(:dueDateFrom AS TIMESTAMP) IS NULL OR t.due_date >= CAST(:dueDateFrom AS TIMESTAMP)) "
                        +
                        "AND (CAST(:dueDateTo AS TIMESTAMP) IS NULL OR t.due_date <= CAST(:dueDateTo AS TIMESTAMP))", countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.is_deleted = false "
                                        +
                                        "AND (CAST(:status AS VARCHAR) IS NULL OR t.status = :status) " +
                                        "AND (CAST(:priority AS VARCHAR) IS NULL OR t.priority = :priority) " +
                                        "AND (CAST(:assignedTo AS UUID) IS NULL OR t.assigned_to = CAST(:assignedTo AS UUID)) "
                                        +
                                        "AND (CAST(:dueDateFrom AS TIMESTAMP) IS NULL OR t.due_date >= CAST(:dueDateFrom AS TIMESTAMP)) "
                                        +
                                        "AND (CAST(:dueDateTo AS TIMESTAMP) IS NULL OR t.due_date <= CAST(:dueDateTo AS TIMESTAMP))", nativeQuery = true)
        Page<Task> findAllWithFilters(
                        @Param("status") String status,
                        @Param("priority") String priority,
                        @Param("assignedTo") UUID assignedTo,
                        @Param("dueDateFrom") LocalDateTime dueDateFrom,
                        @Param("dueDateTo") LocalDateTime dueDateTo,
                        Pageable pageable);

        Page<Task> findByAssignedToAndIsDeletedFalse(UUID assignedTo, Pageable pageable);

        @Query("SELECT t FROM Task t WHERE t.isDeleted = false " +
                        "AND t.dueDate < :now " +
                        "AND t.status NOT IN ('DONE', 'CANCELLED')")
        List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

        @Query("SELECT t FROM Task t WHERE t.isDeleted = false " +
                        "AND t.dueDate < :now " +
                        "AND t.status NOT IN ('DONE', 'CANCELLED')")
        Page<Task> findOverdueTasksPaged(@Param("now") LocalDateTime now, Pageable pageable);

        @Query(value = "SELECT * FROM tasks t WHERE t.is_deleted = false " +
                        "AND to_tsvector('english', COALESCE(t.title, '') || ' ' || COALESCE(t.description, '')) " +
                        "@@ plainto_tsquery('english', :query)", countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.is_deleted = false "
                                        +
                                        "AND to_tsvector('english', COALESCE(t.title, '') || ' ' || COALESCE(t.description, '')) "
                                        +
                                        "@@ plainto_tsquery('english', :query)", nativeQuery = true)
        Page<Task> searchFullText(@Param("query") String query, Pageable pageable);
}

package com.digiwork.taskhive.module.notification.repository;

import com.digiwork.taskhive.module.notification.enums.EmailStatus;
import com.digiwork.taskhive.module.notification.model.EmailQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, UUID> {

    List<EmailQueue> findByStatusAndAttemptsLessThanAndScheduledAtBeforeOrderByScheduledAtAsc(
            EmailStatus status, int maxAttempts, LocalDateTime now, Pageable pageable);
}

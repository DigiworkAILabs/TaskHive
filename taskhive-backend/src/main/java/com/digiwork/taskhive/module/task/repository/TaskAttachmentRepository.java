package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.task.enums.AttachmentPurpose;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {

    List<TaskAttachment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    boolean existsByTaskIdAndAttachmentPurpose(UUID taskId, AttachmentPurpose attachmentPurpose);

    /**
     * Bulk-update attachment purposes for a given task.
     * Used on rejection: PROOF → REJECTED_PROOF (audit trail preserved, employee must re-upload).
     */
    @Modifying
    @Query("UPDATE TaskAttachment a SET a.attachmentPurpose = :newPurpose " +
           "WHERE a.taskId = :taskId AND a.attachmentPurpose = :oldPurpose")
    void updatePurposeByTaskId(
        @Param("taskId") UUID taskId,
        @Param("oldPurpose") AttachmentPurpose oldPurpose,
        @Param("newPurpose") AttachmentPurpose newPurpose);
}

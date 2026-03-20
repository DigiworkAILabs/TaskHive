package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.task.enums.AttachmentPurpose;
import com.digiwork.taskhive.module.task.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {

    List<TaskAttachment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    boolean existsByTaskIdAndAttachmentPurpose(UUID taskId, AttachmentPurpose attachmentPurpose);
}

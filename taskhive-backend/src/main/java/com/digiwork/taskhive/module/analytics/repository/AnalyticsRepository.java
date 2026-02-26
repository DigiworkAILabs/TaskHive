package com.digiwork.taskhive.module.analytics.repository;

import com.digiwork.taskhive.module.analytics.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnalyticsRepository extends JpaRepository<FileMetadata, UUID> {

    // This repository serves as the FileMetadata CRUD repository.
    // Dashboard/chart native queries are placed in the service layer via
    // EntityManager
    // to keep the repository interface clean and avoid mixing concerns between
    // FileMetadata entity and cross-table analytics queries.
}

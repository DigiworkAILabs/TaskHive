package com.digiwork.taskhive.module.ml.repository;

import com.digiwork.taskhive.module.ml.model.MLPredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MLPredictionLogRepository extends JpaRepository<MLPredictionLog, UUID> {

    @Query(value = "SELECT * FROM ml_prediction_logs " +
            "WHERE feature_type = :featureType AND employee_id = :employeeId " +
            "ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<MLPredictionLog> findLatestByFeatureAndEmployee(
            @Param("featureType") String featureType,
            @Param("employeeId") UUID employeeId);
}

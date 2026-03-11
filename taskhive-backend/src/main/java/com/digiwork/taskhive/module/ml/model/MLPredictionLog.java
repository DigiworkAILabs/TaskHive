package com.digiwork.taskhive.module.ml.model;

import com.digiwork.taskhive.module.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ml_prediction_logs")
public class MLPredictionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "feature_type", nullable = false)
    private String featureType; // PRIORITY, COMPLETION, WORKLOAD, PRODUCTIVITY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employee;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data", nullable = false)
    private Map<String, Object> inputData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_data", nullable = false)
    private Map<String, Object> outputData;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "fallback_used")
    private boolean fallbackUsed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

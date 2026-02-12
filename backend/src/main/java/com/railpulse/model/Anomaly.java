package com.railpulse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.railpulse.config.PointSerializer;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomalies", indexes = {
    @Index(name = "idx_train_time", columnList = "train_id, detected_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomaly {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "train_id", nullable = false, length = 50)
    private String trainId;
    
    @Column(name = "anomaly_type", nullable = false, length = 50)
    private String anomalyType;
    
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;
    
    @Column(name = "location", columnDefinition = "geometry(Point, 4326)")
    @JsonSerialize(using = PointSerializer.class)
    private Point location;
    
    @Column(name = "speed_kmh")
    private Double speedKmh;
    
    @Column(name = "vibration_level")
    private Double vibrationLevel;
}
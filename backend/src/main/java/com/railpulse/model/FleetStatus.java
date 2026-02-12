package com.railpulse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Table(name = "fleet_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetStatus {
    
    @Id
    @Column(name = "train_id", length = 50)
    private String trainId;
    
    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;
    
    @Column(name = "current_speed")
    private Double currentSpeed;
    
    @Column(name = "current_location", columnDefinition = "geometry(Point, 4326)")
    private Point currentLocation;
    
    @Column(name = "vibration_level")
    private Double vibrationLevel;
    
    @Column(name = "engine_temp")
    private Double engineTemp;
    
    @Column(name = "status", length = 20)
    private String status;
}
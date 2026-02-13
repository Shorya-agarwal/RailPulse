package com.railpulse.streams;

import com.railpulse.model.Anomaly;
import com.railpulse.model.FleetStatus;
import com.railpulse.model.Telemetry;
import com.railpulse.repository.AnomalyRepository;
import com.railpulse.repository.FleetStatusRepository;
import com.railpulse.streams.service.WebSocketService; 
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeospatialProcessor {
    
    private final AnomalyRepository anomalyRepository;
    private final FleetStatusRepository fleetStatusRepository;
    private final WebSocketService webSocketService;  // ADD THIS
    private final GeometryFactory geometryFactory;
    
    @Value("${railpulse.vibration.critical-threshold}")
    private double criticalVibrationThreshold;
    
    @Value("${railpulse.vibration.warning-threshold}")
    private double warningVibrationThreshold;
    
    @Value("${railpulse.speed.curve-threshold}")
    private double curveSpeedThreshold;
    
    private static final double CURVE_LAT_MIN = 30.25;
    private static final double CURVE_LAT_MAX = 30.26;
    private static final double CURVE_LON_MIN = -97.75;
    private static final double CURVE_LON_MAX = -97.74;
    
    public GeospatialProcessor(AnomalyRepository anomalyRepository, 
                               FleetStatusRepository fleetStatusRepository,
                               WebSocketService webSocketService) {  // ADD webSocketService parameter
        this.anomalyRepository = anomalyRepository;
        this.fleetStatusRepository = fleetStatusRepository;
        this.webSocketService = webSocketService;  // ADD THIS
        this.geometryFactory = new GeometryFactory();
    }
    
    public void processTelemetry(Telemetry telemetry) {
        try {
            checkVibrationAnomaly(telemetry);
            checkCurveSpeedAnomaly(telemetry);
            updateFleetStatus(telemetry);
        } catch (Exception e) {
            log.error("Error processing telemetry for train {}: {}", 
                     telemetry.getTrainId(), e.getMessage());
        }
    }
    
    private void checkVibrationAnomaly(Telemetry telemetry) {
        double vibration = telemetry.getVibrationLevel();
        
        if (vibration >= criticalVibrationThreshold) {
            Anomaly anomaly = Anomaly.builder()
                .trainId(telemetry.getTrainId())
                .anomalyType("HIGH_VIBRATION")
                .severity("CRITICAL")
                .description(String.format(
                    "Critical vibration level %.2f detected (threshold: %.2f)",
                    vibration, criticalVibrationThreshold
                ))
                .detectedAt(telemetry.getTimestamp())
                .location(createPoint(telemetry.getLatitude(), telemetry.getLongitude()))
                .speedKmh(telemetry.getSpeedKmh())
                .vibrationLevel(vibration)
                .build();
            
            anomalyRepository.save(anomaly);
            webSocketService.broadcastAnomaly(anomaly);  // ADD THIS LINE
            log.warn("🚨 CRITICAL: Train {} - High vibration {}", 
                    telemetry.getTrainId(), vibration);
        } 
        else if (vibration >= warningVibrationThreshold) {
            Anomaly anomaly = Anomaly.builder()
                .trainId(telemetry.getTrainId())
                .anomalyType("ELEVATED_VIBRATION")
                .severity("WARNING")
                .description(String.format(
                    "Elevated vibration level %.2f detected", vibration
                ))
                .detectedAt(telemetry.getTimestamp())
                .location(createPoint(telemetry.getLatitude(), telemetry.getLongitude()))
                .speedKmh(telemetry.getSpeedKmh())
                .vibrationLevel(vibration)
                .build();
            
            anomalyRepository.save(anomaly);
            webSocketService.broadcastAnomaly(anomaly);  // ADD THIS LINE
            log.info("⚠️ WARNING: Train {} - Elevated vibration {}", 
                    telemetry.getTrainId(), vibration);
        }
    }
    
    private void checkCurveSpeedAnomaly(Telemetry telemetry) {
        double lat = telemetry.getLatitude();
        double lon = telemetry.getLongitude();
        double speed = telemetry.getSpeedKmh();
        
        boolean inCurve = lat >= CURVE_LAT_MIN && lat <= CURVE_LAT_MAX &&
                          lon >= CURVE_LON_MIN && lon <= CURVE_LON_MAX;
        
        if (inCurve && speed > curveSpeedThreshold) {
            Anomaly anomaly = Anomaly.builder()
                .trainId(telemetry.getTrainId())
                .anomalyType("DERAILMENT_RISK")
                .severity("CRITICAL")
                .description(String.format(
                    "Speeding in sharp curve: %.2f km/h (safe limit: %.2f km/h)",
                    speed, curveSpeedThreshold
                ))
                .detectedAt(telemetry.getTimestamp())
                .location(createPoint(lat, lon))
                .speedKmh(speed)
                .vibrationLevel(telemetry.getVibrationLevel())
                .build();
            
            anomalyRepository.save(anomaly);
            webSocketService.broadcastAnomaly(anomaly);  // ADD THIS LINE
            log.error("🚨 CRITICAL: Train {} - Speeding in curve {} km/h", 
                     telemetry.getTrainId(), speed);
        }
    }
    
    private void updateFleetStatus(Telemetry telemetry) {
        FleetStatus status = fleetStatusRepository.findById(telemetry.getTrainId())
            .orElse(new FleetStatus());
        
        status.setTrainId(telemetry.getTrainId());
        status.setLastSeen(telemetry.getTimestamp());
        status.setCurrentSpeed(telemetry.getSpeedKmh());
        status.setCurrentLocation(createPoint(telemetry.getLatitude(), telemetry.getLongitude()));
        status.setVibrationLevel(telemetry.getVibrationLevel());
        status.setEngineTemp(telemetry.getEngineTemp());
        
        if (telemetry.getVibrationLevel() >= criticalVibrationThreshold) {
            status.setStatus("DANGER");
        } else if (telemetry.getSpeedKmh() < 5.0) {
            status.setStatus("STOPPED");
        } else {
            status.setStatus("MOVING");
        }
        
        fleetStatusRepository.save(status);
        webSocketService.broadcastFleetStatus(status);  // ADD THIS LINE
    }
    
    private Point createPoint(double lat, double lon) {
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}
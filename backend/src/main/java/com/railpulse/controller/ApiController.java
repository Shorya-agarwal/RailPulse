package com.railpulse.controller;

import com.railpulse.model.Anomaly;
import com.railpulse.model.FleetStatus;
import com.railpulse.repository.AnomalyRepository;
import com.railpulse.repository.FleetStatusRepository;
import com.railpulse.streams.service.ArchivalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // For development; restrict in production
@Slf4j
public class ApiController {
    
    private final AnomalyRepository anomalyRepository;
    private final FleetStatusRepository fleetStatusRepository;
    private final ArchivalService archivalService;
    
    public ApiController(AnomalyRepository anomalyRepository,
                        FleetStatusRepository fleetStatusRepository,
                        ArchivalService archivalService) {
        this.anomalyRepository = anomalyRepository;
        this.fleetStatusRepository = fleetStatusRepository;
        this.archivalService = archivalService;
    }
    
    /**
     * GET /api/anomalies - Get latest anomalies
     */
    @GetMapping("/anomalies")
    public ResponseEntity<List<Anomaly>> getLatestAnomalies(
            @RequestParam(defaultValue = "10") int limit) {
        List<Anomaly> anomalies = anomalyRepository.findTop10ByOrderByDetectedAtDesc();
        return ResponseEntity.ok(anomalies);
    }
    
    /**
     * GET /api/anomalies/recent - Get anomalies from last N minutes
     */
    @GetMapping("/anomalies/recent")
    public ResponseEntity<List<Anomaly>> getRecentAnomalies(
            @RequestParam(defaultValue = "5") int minutes) {
        Instant since = Instant.now().minus(minutes, ChronoUnit.MINUTES);
        List<Anomaly> anomalies = anomalyRepository.findRecentAnomalies(since);
        return ResponseEntity.ok(anomalies);
    }
    
    /**
     * GET /api/anomalies/train/{trainId} - Get anomalies for specific train
     */
    @GetMapping("/anomalies/train/{trainId}")
    public ResponseEntity<List<Anomaly>> getAnomaliesByTrain(
            @PathVariable String trainId) {
        List<Anomaly> anomalies = anomalyRepository.findByTrainIdOrderByDetectedAtDesc(trainId);
        return ResponseEntity.ok(anomalies);
    }
    
    /**
     * GET /api/fleet - Get all fleet status
     */
    @GetMapping("/fleet")
    public ResponseEntity<List<FleetStatus>> getFleetStatus() {
        List<FleetStatus> fleet = fleetStatusRepository.findAll();
        return ResponseEntity.ok(fleet);
    }
    
    /**
     * GET /api/fleet/{trainId} - Get status for specific train
     */
    @GetMapping("/fleet/{trainId}")
    public ResponseEntity<FleetStatus> getTrainStatus(@PathVariable String trainId) {
        return fleetStatusRepository.findById(trainId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/fleet/status/{status} - Get trains by status (MOVING, STOPPED, DANGER)
     */
    @GetMapping("/fleet/status/{status}")
    public ResponseEntity<List<FleetStatus>> getFleetByStatus(@PathVariable String status) {
        List<FleetStatus> fleet = fleetStatusRepository.findByStatus(status.toUpperCase());
        return ResponseEntity.ok(fleet);
    }
    
    /**
     * GET /api/stats - Get system statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count totals
        stats.put("totalAnomalies", anomalyRepository.count());
        stats.put("totalTrains", fleetStatusRepository.count());
        stats.put("trainsMoving", fleetStatusRepository.findByStatus("MOVING").size());
        stats.put("trainsStopped", fleetStatusRepository.findByStatus("STOPPED").size());
        stats.put("trainsInDanger", fleetStatusRepository.findByStatus("DANGER").size());
        
        // Archival stats
        stats.put("totalArchived", archivalService.getTotalArchived());
        stats.put("bufferSize", archivalService.getBufferSize());
        
        // Recent anomalies count
        Instant lastHour = Instant.now().minus(1, ChronoUnit.HOURS);
        stats.put("anomaliesLastHour", anomalyRepository.findRecentAnomalies(lastHour).size());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/health - Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(health);
    }
}
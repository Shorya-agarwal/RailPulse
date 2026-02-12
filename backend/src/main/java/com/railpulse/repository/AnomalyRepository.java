package com.railpulse.repository;

import com.railpulse.model.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, UUID> {
    
    List<Anomaly> findTop10ByOrderByDetectedAtDesc();
    
    List<Anomaly> findByTrainIdOrderByDetectedAtDesc(String trainId);
    
    @Query("SELECT a FROM Anomaly a WHERE a.detectedAt >= :since ORDER BY a.detectedAt DESC")
    List<Anomaly> findRecentAnomalies(Instant since);
}
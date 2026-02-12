package com.railpulse.repository;

import com.railpulse.model.FleetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FleetStatusRepository extends JpaRepository<FleetStatus, String> {
    
    List<FleetStatus> findByStatus(String status);
}
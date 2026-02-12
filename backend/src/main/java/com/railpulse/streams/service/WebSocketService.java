package com.railpulse.streams.service;

import com.railpulse.model.Anomaly;
import com.railpulse.model.FleetStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Broadcast anomaly to all connected WebSocket clients
     */
    public void broadcastAnomaly(Anomaly anomaly) {
        try {
            messagingTemplate.convertAndSend("/topic/anomalies", anomaly);
            log.debug("Broadcasted anomaly: {} for train {}", 
                     anomaly.getAnomalyType(), anomaly.getTrainId());
        } catch (Exception e) {
            log.error("Failed to broadcast anomaly via WebSocket: {}", e.getMessage());
        }
    }
    
    /**
     * Broadcast fleet status update to all connected clients
     */
    public void broadcastFleetStatus(FleetStatus status) {
        try {
            messagingTemplate.convertAndSend("/topic/fleet-status", status);
            log.debug("Broadcasted fleet status for train {}", status.getTrainId());
        } catch (Exception e) {
            log.error("Failed to broadcast fleet status via WebSocket: {}", e.getMessage());
        }
    }
}
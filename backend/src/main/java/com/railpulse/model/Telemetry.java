package com.railpulse.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Telemetry {
    
    @JsonProperty("train_id")
    private String trainId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("speed_kmh")
    private Double speedKmh;
    
    @JsonProperty("vibration_level")
    private Double vibrationLevel;
    
    @JsonProperty("engine_temp")
    private Double engineTemp;
}
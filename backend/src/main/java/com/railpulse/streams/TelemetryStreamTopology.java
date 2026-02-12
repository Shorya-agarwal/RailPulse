package com.railpulse.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.railpulse.model.Telemetry;
import com.railpulse.streams.service.ArchivalService; 
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TelemetryStreamTopology {
    
    private final GeospatialProcessor geospatialProcessor;
    private final ArchivalService archivalService;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.kafka.consumer.topic:telemetry-stream}")
    private String inputTopic;
    
    @Autowired
    public TelemetryStreamTopology(GeospatialProcessor geospatialProcessor,
                                    ArchivalService archivalService) { 
        this.geospatialProcessor = geospatialProcessor;
        this.archivalService = archivalService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Define Kafka Streams topology
     * This method is auto-detected by Spring Kafka and builds the processing pipeline
     */
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        // Create stream from input topic
        KStream<String, String> telemetryStream = streamsBuilder.stream(
            inputTopic,
            Consumed.with(Serdes.String(), Serdes.String())
        );
        
        // Process each telemetry event
        telemetryStream
            .peek((key, value) -> log.debug("Received event from train: {}", key))
            
            // Parse JSON to Telemetry object
            .mapValues(this::parseTelemetry)
            
            // Filter out parsing errors (null values)
            .filter((key, telemetry) -> telemetry != null)
            
            // Process telemetry (anomaly detection + fleet status update)
            .foreach((key, telemetry) -> {
                try {
                     // Hot path: Anomaly detection + fleet status
                    geospatialProcessor.processTelemetry(telemetry);
                    
                     // Cold path: Archive to MinIO
                    archivalService.addToBuffer(telemetry);
                } catch (Exception e) {
                    log.error("Error processing telemetry for train {}: {}", 
                             key, e.getMessage(), e);
                }
            });
        
        log.info("Kafka Streams topology built successfully for topic: {}", inputTopic);
    }
    
    /**
     * Parse JSON string to Telemetry object
     */
    private Telemetry parseTelemetry(String json) {
        try {
            return objectMapper.readValue(json, Telemetry.class);
        } catch (Exception e) {
            log.error("Failed to parse telemetry JSON: {}", json, e);
            return null;
        }
    }
}
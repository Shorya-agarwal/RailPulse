package com.railpulse.streams.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.railpulse.model.Telemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class ArchivalService {
    
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final ReentrantLock lock = new ReentrantLock();
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${railpulse.archival.batch-size}")
    private int batchSize;
    
    // In-memory buffer for batching
    private final List<Telemetry> telemetryBuffer = new ArrayList<>();
    private long totalArchived = 0;
    
    public ArchivalService(S3Client s3Client, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Add telemetry to buffer for archival
     */
    public void addToBuffer(Telemetry telemetry) {
        lock.lock();
        try {
            telemetryBuffer.add(telemetry);
            
            // Flush if batch size reached
            if (telemetryBuffer.size() >= batchSize) {
                flushBuffer();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Scheduled task to flush buffer every 60 seconds
     */
    @Scheduled(fixedDelayString = "${railpulse.archival.interval-seconds}000")
    public void scheduledFlush() {
        lock.lock();
        try {
            if (!telemetryBuffer.isEmpty()) {
                flushBuffer();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Flush buffer to MinIO
     */
    private void flushBuffer() {
        if (telemetryBuffer.isEmpty()) {
            return;
        }
        
        try {
            // Generate partitioned key: telemetry/date=2026-02-12/batch-<timestamp>.json
            String datePartition = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String timestamp = Instant.now().toEpochMilli() + "";
            String key = String.format("telemetry/date=%s/batch-%s.json", datePartition, timestamp);
            
            // Convert buffer to JSON
            String jsonData = objectMapper.writeValueAsString(telemetryBuffer);
            
            // Upload to MinIO
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromString(jsonData));
            
            totalArchived += telemetryBuffer.size();
            log.info("Archived {} telemetry records to MinIO (key: {}). Total archived: {}",
                    telemetryBuffer.size(), key, totalArchived);
            
            // Clear buffer
            telemetryBuffer.clear();
            
        } catch (Exception e) {
            log.error("Failed to archive telemetry to MinIO: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get statistics
     */
    public long getTotalArchived() {
        return totalArchived;
    }
    
    public int getBufferSize() {
        lock.lock();
        try {
            return telemetryBuffer.size();
        } finally {
            lock.unlock();
        }
    }
}
package com.railpulse.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Value("${spring.kafka.consumer.topic:telemetry-stream}")
    private String telemetryTopic;
    
    /**
     * Auto-create telemetry-stream topic if it doesn't exist
     */
    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name(telemetryTopic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000))  // 7 days
                .config("compression.type", "snappy")
                .build();
    }
}
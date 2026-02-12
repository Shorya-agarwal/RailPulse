package com.railpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
@EnableAsync
@EnableScheduling
public class RailPulseApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RailPulseApplication.class, args);
        System.out.println("RailPulse Backend Started Successfully");
    }
}
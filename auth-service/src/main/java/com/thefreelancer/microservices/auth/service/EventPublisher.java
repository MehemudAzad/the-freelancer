package com.thefreelancer.microservices.auth.service;

import com.thefreelancer.microservices.auth.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String USER_EVENTS_TOPIC = "user-events";
    
    public void publishUserCreated(UserCreatedEvent event) {
        try {
            kafkaTemplate.send(USER_EVENTS_TOPIC, event.getUserId().toString(), event);
            log.info("Published UserCreatedEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for userId: {}", event.getUserId(), e);
        }
    }
}

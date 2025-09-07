package com.thefreelancer.microservices.gig.listener;

import com.thefreelancer.microservices.gig.event.UserCreatedEvent;
import com.thefreelancer.microservices.gig.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    
    private final ProfileService profileService;
    
    @KafkaListener(topics = "user-events", groupId = "gig-service-group")
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            log.info("Received UserCreatedEvent for userId: {}", event.getUserId());
            
            // Create initial profile for the new user
            profileService.createInitialProfile(
                    event.getUserId(),
                    event.getName(),
                    event.getRole()
            );
            
            log.info("Successfully processed UserCreatedEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process UserCreatedEvent for userId: {}", event.getUserId(), e);
        }
    }
}

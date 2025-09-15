package com.thefreelancer.microservices.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private Long recipientId;
    private Long senderId;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private String status;
    private Long jobId;
    private Long proposalId;
    private Long contractId;
    private Long milestoneId;
    private String referenceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Enriched data from other services
    private SenderInfo senderInfo;
    private JobInfo jobInfo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SenderInfo {
        private Long id;
        private String name;
        private String email;
        private String handle;
        private String role;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobInfo {
        private Long id;
        private String title;
        private String status;
    }
}

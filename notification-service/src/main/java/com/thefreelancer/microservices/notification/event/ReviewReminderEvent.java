package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReminderEvent {
    private String clientId;
    private String freelancerId;
    private String jobId;
    private String jobTitle;
    private String freelancerName;
    private String contractId;
    private Long timestamp;
}
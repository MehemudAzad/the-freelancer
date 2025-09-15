package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.notification.email.from}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    public void sendNotificationEmail(Notification notification) {
        if (!emailEnabled) {
            log.debug("Email notifications are disabled");
            return;
        }
        
        try {
            // TODO: Get recipient email from auth-service
            String recipientEmail = fetchUserEmail(notification.getRecipientId());
            
            if (recipientEmail == null || recipientEmail.isEmpty()) {
                log.warn("No email found for user {}", notification.getRecipientId());
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(notification.getTitle());
            message.setText(buildEmailBody(notification));
            
            mailSender.send(message);
            log.debug("Email sent successfully to {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String buildEmailBody(Notification notification) {
        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append(notification.getMessage()).append("\n\n");
        
        if (notification.getJobId() != null) {
            body.append("Job ID: ").append(notification.getJobId()).append("\n");
        }
        
        body.append("\nYou can view more details by logging into your FreelancerHub account.\n\n");
        body.append("Best regards,\n");
        body.append("FreelancerHub Team");
        
        return body.toString();
    }
    
    private String fetchUserEmail(Long userId) {
        // TODO: Implement call to auth-service to get user email
        // This is a placeholder
        return "user" + userId + "@example.com";
    }
}

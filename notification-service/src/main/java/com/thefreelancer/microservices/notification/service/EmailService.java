package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.client.AuthServiceClient;
import com.thefreelancer.microservices.notification.client.UserResponse;
import com.thefreelancer.microservices.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final AuthServiceClient authServiceClient;
    
    @Value("${app.notification.email.from:noreply@freelancerhub.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public void sendNotificationEmail(Notification notification) {
        if (!emailEnabled) {
            log.debug("Email notifications are disabled");
            return;
        }
        
        try {
            UserResponse recipient = authServiceClient.getUserById(notification.getRecipientId());

            if (recipient == null || recipient.getEmail() == null || recipient.getEmail().isEmpty()) {
                log.warn("No email found for user {}", notification.getRecipientId());
                return;
            }

            // Send HTML email for better formatting
            sendHtmlEmail(recipient, notification);
            log.debug("Email sent successfully to {}", recipient.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send email notification due to messaging error: {}", e.getMessage(), e);
            // Optionally, you can wrap in a runtime exception or just return
            // throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void sendHtmlEmail(UserResponse recipient, Notification notification) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setFrom(fromEmail);
        helper.setTo(recipient.getEmail());
        helper.setSubject(buildEmailSubject(notification));
        helper.setText(buildHtmlEmailBody(recipient, notification), true);
        
        mailSender.send(message);
    }
    
    private String buildEmailSubject(Notification notification) {
        return switch (notification.getType()) {
            case PROPOSAL_SUBMITTED -> "New Proposal Received - FreelancerHub";
            case PROPOSAL_ACCEPTED -> "Proposal Accepted! - FreelancerHub";
            case PROPOSAL_REJECTED -> "Proposal Update - FreelancerHub";
            case CONTRACT_CREATED -> "Contract Created - FreelancerHub";
            case JOB_SUBMITTED -> "Job Submitted for Review - FreelancerHub";
            case PAYMENT_RECEIVED -> "Payment Received - FreelancerHub";
            case MESSAGE_RECEIVED -> "New Message - FreelancerHub";
            default -> notification.getTitle() + " - FreelancerHub";
        };
    }
    
    private String buildHtmlEmailBody(UserResponse recipient, Notification notification) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset='UTF-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            .append("<title>").append(notification.getTitle()).append("</title>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
            .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
            .append(".header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }")
            .append(".content { background-color: #f8fafc; padding: 30px; }")
            .append(".button { display: inline-block; background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0; }")
            .append(".footer { background-color: #64748b; color: white; padding: 15px; text-align: center; font-size: 12px; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class='container'>")
            .append("<div class='header'>")
            .append("<h1>FreelancerHub</h1>")
            .append("</div>")
            .append("<div class='content'>")
            .append("<h2>").append(notification.getTitle()).append("</h2>")
            .append("<p>Hello ").append(recipient.getName() != null ? recipient.getName() : "User").append(",</p>")
            .append("<p>").append(notification.getMessage()).append("</p>");
        
        // Add notification-specific content
        switch (notification.getType()) {
            case PROPOSAL_SUBMITTED:
                html.append("<p>A new proposal has been submitted for your job. Review it now to find the perfect freelancer for your project.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/proposals' class='button'>View Proposals</a>");
                }
                break;
                
            case PROPOSAL_ACCEPTED:
                html.append("<p>Congratulations! Your proposal has been accepted. You can now start working on this exciting project.</p>");
                if (notification.getContractId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/contracts/").append(notification.getContractId()).append("' class='button'>View Contract</a>");
                }
                break;
                
            case CONTRACT_CREATED:
                html.append("<p>A new contract has been created. You can now collaborate and track progress in your workspace.</p>");
                if (notification.getContractId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/workspace/").append(notification.getContractId()).append("' class='button'>Open Workspace</a>");
                }
                break;
                
            case JOB_SUBMITTED:
                html.append("<p>A job has been submitted and is ready for your review. Please check the details and provide feedback.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("' class='button'>Review Job</a>");
                }
                break;
                
            case PAYMENT_RECEIVED:
                html.append("<p>Great news! Your payment has been processed and should appear in your account soon.</p>");
                html.append("<a href='").append(frontendUrl).append("/dashboard/earnings' class='button'>View Earnings</a>");
                break;
                
            case MESSAGE_RECEIVED:
                html.append("<p>You have a new message in your workspace. Stay connected with your project team.</p>");
                if (notification.getMetadata() != null && notification.getMetadata().contains("roomId")) {
                    try {
                        String roomId = notification.getMetadata().substring(
                            notification.getMetadata().indexOf("roomId\":") + 8,
                            notification.getMetadata().indexOf("}", notification.getMetadata().indexOf("roomId\":"))
                        );
                        html.append("<a href='").append(frontendUrl).append("/workspace/room/").append(roomId).append("' class='button'>View Message</a>");
                    } catch (Exception e) {
                        log.warn("Could not parse roomId from metadata: {}", notification.getMetadata());
                    }
                }
                break;
                
            default:
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("' class='button'>View Job</a>");
                } else {
                    html.append("<a href='").append(frontendUrl).append("/dashboard' class='button'>View Dashboard</a>");
                }
        }
        
        html.append("<br><br>")
            .append("<p>You can manage your notification preferences in your account settings.</p>")
            .append("<p>Best regards,<br>The FreelancerHub Team</p>")
            .append("</div>")
            .append("<div class='footer'>")
            .append("<p>&copy; 2025 FreelancerHub. All rights reserved.</p>")
            .append("<p>You received this email because you have an account with FreelancerHub.</p>")
            .append("<a href='").append(frontendUrl).append("/settings/notifications' style='color: #cbd5e1;'>Unsubscribe</a>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    // Send simple text email as fallback
    private void sendSimpleEmail(UserResponse recipient, Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipient.getEmail());
        message.setSubject(buildEmailSubject(notification));
        message.setText(buildTextEmailBody(recipient, notification));
        
        mailSender.send(message);
    }
    
    private String buildTextEmailBody(UserResponse recipient, Notification notification) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(recipient.getName() != null ? recipient.getName() : "User").append(",\n\n");
        body.append(notification.getMessage()).append("\n\n");
        
        if (notification.getJobId() != null) {
            body.append("Job ID: ").append(notification.getJobId()).append("\n");
        }
        if (notification.getContractId() != null) {
            body.append("Contract ID: ").append(notification.getContractId()).append("\n");
        }
        
        body.append("\nYou can view more details by logging into your FreelancerHub account at: ")
            .append(frontendUrl).append("\n\n");
        body.append("Best regards,\n");
        body.append("The FreelancerHub Team\n\n");
        body.append("You received this email because you have an account with FreelancerHub.\n");
        body.append("Manage your notification preferences: ").append(frontendUrl).append("/settings/notifications");
        
        return body.toString();
    }
}

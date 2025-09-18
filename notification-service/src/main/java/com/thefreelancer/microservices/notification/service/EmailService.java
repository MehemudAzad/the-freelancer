package com.thefreelancer.microservices.notification.service;

import com.thefreelancer.microservices.notification.client.AuthServiceClient;
import com.thefreelancer.microservices.notification.dto.UserResponse;
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
        
        // Only send emails for specific notification types as per requirements
        if (!shouldSendEmail(notification.getType())) {
            log.debug("Email not required for notification type: {}", notification.getType());
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
    
    /**
     * Determines if an email should be sent for the given notification type.
     * Based on requirements:
     * - INVITE_ACCEPTED (email) - #2
     * - INVITE_RECEIVED (email) - #3  
     * - PROPOSAL_ACCEPTED (email) - #4
     * - ESCROW_FUNDED (email) - #6
     * - JOB_SUBMITTED (email) - #7
     * - JOB_ACCEPTED (email) - #9
     * - REVIEW_REMINDER (email) - #10
     * 
     * All other notifications go to inbox only.
     */
    private boolean shouldSendEmail(Notification.NotificationType notificationType) {
        return switch (notificationType) {
            case INVITE_ACCEPTED,       // #2: invite accepted -> client (email)
                 INVITE_RECEIVED,       // #3: you have an invite -> freelancer (email)
                 PROPOSAL_ACCEPTED,     // #4: proposal accepted -> freelancer (email)
                 ESCROW_FUNDED,         // #6: payment escrow made -> client (email)
                 JOB_SUBMITTED,         // #7: job submitted -> client (email)
                 JOB_ACCEPTED,          // #9: job accepted -> freelancer (email)
                 REVIEW_REMINDER        // #10: review reminder -> client (email)
                 -> true;
            case INVITE_SENT,           // #1: invite sent -> client (inbox only)
                 PROPOSAL_SUBMITTED,    // #5: proposal submitted -> client (inbox only)
                 JOB_REJECTED           // #8: job rejected -> freelancer (inbox only)
                 -> false;
            default -> false;
        };
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
            case INVITE_SENT -> "Invitation Sent - FreelancerHub";
            case INVITE_ACCEPTED -> "Invitation Accepted! - FreelancerHub";
            case INVITE_RECEIVED -> "You Have an Invitation! - FreelancerHub";
            case PROPOSAL_SUBMITTED -> "New Proposal Received - FreelancerHub";
            case PROPOSAL_ACCEPTED -> "Proposal Accepted! - FreelancerHub";
            case ESCROW_FUNDED -> "Payment Escrow Created - FreelancerHub";
            case JOB_SUBMITTED -> "Job Submitted for Review - FreelancerHub";
            case JOB_REJECTED -> "Job Revision Requested - FreelancerHub";
            case JOB_ACCEPTED -> "Job Accepted - Payment Transferred! - FreelancerHub";
            case REVIEW_REMINDER -> "Please Review Freelancer - FreelancerHub";
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
            case INVITE_SENT:
                html.append("<p>You have successfully sent an invitation to work on your project.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("' class='button'>View Job</a>");
                }
                break;
                
            case INVITE_ACCEPTED:
                html.append("<p>Great news! The freelancer has accepted your invitation and is ready to work on your project.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("' class='button'>Start Project</a>");
                }
                break;
                
            case INVITE_RECEIVED:
                html.append("<p>You have been personally invited to work on this exciting project. Review the details and accept if you're interested!</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/invite' class='button'>View Invitation</a>");
                }
                break;
                
            case PROPOSAL_SUBMITTED:
                html.append("<p>A new proposal has been submitted for your job. Review it now to find the perfect freelancer for your project.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/proposals' class='button'>View Proposals</a>");
                }
                break;
                
            case PROPOSAL_ACCEPTED:
                html.append("<p>Congratulations! Your proposal has been accepted. You can now start working on this exciting project.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/contract' class='button'>View Contract</a>");
                }
                break;
                
            case ESCROW_FUNDED:
                html.append("<p>Payment has been secured in escrow for this project. Work can now begin!</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/workspace' class='button'>Open Workspace</a>");
                }
                break;
                
            case JOB_SUBMITTED:
                html.append("<p>A job has been submitted and is ready for your review. Please check the details and provide feedback.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/review' class='button'>Review Job</a>");
                }
                break;
                
            case JOB_REJECTED:
                html.append("<p>Your job submission needs some revisions. Please review the feedback and resubmit when ready.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/feedback' class='button'>View Feedback</a>");
                }
                break;
                
            case JOB_ACCEPTED:
                html.append("<p>Excellent work! Your job submission has been accepted. Payment has been transferred to your account!</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("' class='button'>View Job</a>");
                }
                html.append("<a href='").append(frontendUrl).append("/dashboard/earnings' class='button'>View Earnings</a>");
                break;
                
            case REVIEW_REMINDER:
                html.append("<p>Your project has been completed successfully. Please take a moment to review the freelancer to help our community.</p>");
                if (notification.getJobId() != null) {
                    html.append("<a href='").append(frontendUrl).append("/jobs/").append(notification.getJobId()).append("/review-freelancer' class='button'>Leave Review</a>");
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
}

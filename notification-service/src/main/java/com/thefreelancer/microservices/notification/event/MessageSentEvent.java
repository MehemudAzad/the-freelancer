package com.thefreelancer.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageSentEvent {
    private Long messageId;
    private Long senderId;
    private Long recipientId;
    private Long roomId;
    private String senderName;
    private String messageContent;
    private String messageType;
    private LocalDateTime sentAt;
    private boolean isEdited;
    private Long replyToMessageId;
}
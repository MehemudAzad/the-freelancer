package com.thefreelancer.microservices.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDto {
    
    private String conversationId; // Generated ID for the conversation
    private String otherParticipantId; // The other user in the conversation
    private String otherParticipantName; // Name of the other user
    private String otherParticipantHandle; // Handle/username of the other user
    private DirectMessageResponseDto lastMessage; // Most recent message in the conversation
    private long unreadCount; // Number of unread messages for current user
    private LocalDateTime lastActivity; // Timestamp of the most recent message
    private List<String> participantIds; // All participants (for group DMs in the future)
    
    // Helper method to check if conversation has unread messages
    public boolean hasUnreadMessages() {
        return unreadCount > 0;
    }
    
    // Helper method to get conversation display name
    public String getDisplayName() {
        return otherParticipantName != null ? otherParticipantName : otherParticipantHandle;
    }
}

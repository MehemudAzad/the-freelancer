package com.thefreelancer.microservices.workspace_service.repository;

import com.thefreelancer.microservices.workspace_service.model.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, String> {
    
    // Find conversation messages between two users with pagination
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
           "ORDER BY m.createdAt DESC")
    Page<DirectMessage> findConversationMessages(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2, 
        Pageable pageable);
    
    // Find conversation messages before a specific message (for cursor pagination)
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) AND " +
           "m.createdAt < (SELECT msg.createdAt FROM DirectMessage msg WHERE msg.id = :beforeMessageId) " +
           "ORDER BY m.createdAt DESC")
    Page<DirectMessage> findConversationMessagesBeforeMessage(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2,
        @Param("beforeMessageId") String beforeMessageId,
        Pageable pageable);
    
    // Find conversation messages after a specific message (for real-time updates)
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) AND " +
           "m.createdAt > (SELECT msg.createdAt FROM DirectMessage msg WHERE msg.id = :afterMessageId) " +
           "ORDER BY m.createdAt ASC")
    List<DirectMessage> findConversationMessagesAfterMessage(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2,
        @Param("afterMessageId") String afterMessageId);
    
    // Get all conversations for a user (distinct other participants)
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END " +
           "FROM DirectMessage m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<String> findConversationParticipants(@Param("userId") String userId);
    
    // Get latest message for each conversation of a user
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "(m.senderId = :userId OR m.receiverId = :userId) AND " +
           "m.createdAt = (SELECT MAX(dm.createdAt) FROM DirectMessage dm WHERE " +
           "((dm.senderId = :userId AND dm.receiverId = " +
           "CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END) OR " +
           "(dm.receiverId = :userId AND dm.senderId = " +
           "CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END))) " +
           "ORDER BY m.createdAt DESC")
    List<DirectMessage> findLatestMessagesForUserConversations(@Param("userId") String userId);
    
    // Count unread messages for a user from a specific sender
    @Query("SELECT COUNT(m) FROM DirectMessage m WHERE " +
           "m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false")
    long countUnreadMessagesFromSender(
        @Param("receiverId") String receiverId, 
        @Param("senderId") String senderId);
    
    // Count total unread messages for a user
    @Query("SELECT COUNT(m) FROM DirectMessage m WHERE " +
           "m.receiverId = :receiverId AND m.isRead = false")
    long countTotalUnreadMessages(@Param("receiverId") String receiverId);
    
    // Find unread messages for a user
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "m.receiverId = :receiverId AND m.isRead = false " +
           "ORDER BY m.createdAt ASC")
    List<DirectMessage> findUnreadMessagesForUser(@Param("receiverId") String receiverId);
    
    // Mark messages as read between two users
    @Modifying
    @Query("UPDATE DirectMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE " +
           "m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false")
    int markMessagesAsReadBetweenUsers(
        @Param("receiverId") String receiverId, 
        @Param("senderId") String senderId);
    
    // Mark a specific message as read
    @Modifying
    @Query("UPDATE DirectMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE " +
           "m.id = :messageId AND m.receiverId = :receiverId")
    int markMessageAsRead(@Param("messageId") String messageId, @Param("receiverId") String receiverId);
    
    // Search messages in a conversation
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) AND " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY m.createdAt DESC")
    Page<DirectMessage> searchConversationMessages(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2,
        @Param("searchTerm") String searchTerm, 
        Pageable pageable);
    
    // Find message with reply context
    @Query("SELECT m FROM DirectMessage m LEFT JOIN FETCH m.replyToMessage WHERE m.id = :messageId")
    Optional<DirectMessage> findByIdWithReplyContext(@Param("messageId") String messageId);
    
    // Find latest message in a conversation
    @Query("SELECT m FROM DirectMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    Optional<DirectMessage> findLatestMessageInConversation(
        @Param("userId1") String userId1, 
        @Param("userId2") String userId2);
    
    // Count messages in a conversation
    @Query("SELECT COUNT(m) FROM DirectMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1))")
    long countConversationMessages(@Param("userId1") String userId1, @Param("userId2") String userId2);
    
    // Find messages sent by a specific user
    Page<DirectMessage> findBySenderIdOrderByCreatedAtDesc(String senderId, Pageable pageable);
    
    // Find messages received by a specific user
    Page<DirectMessage> findByReceiverIdOrderByCreatedAtDesc(String receiverId, Pageable pageable);
}

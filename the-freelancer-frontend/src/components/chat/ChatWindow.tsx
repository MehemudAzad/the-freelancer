import React, { useState, useEffect, useRef } from 'react';
import { Conversation, Message, User } from '@/types/api';
import { ChatService } from '@/services/chat';
import userService from '@/services/user';
import { Send, Paperclip, Smile, MoreVertical, Edit3, Check, X, Trash2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface ChatWindowProps {
  conversation: Conversation;
  currentUser: User | null;
  onMessageSent: (message: Message) => void;
}

// Avatar component with profile picture and fallback
const UserAvatar: React.FC<{
  user: User | null;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}> = ({ user, size = 'md', className = '' }) => {
  const [imageError, setImageError] = useState(false);

  const sizeClasses = {
    sm: 'w-8 h-8 text-sm',
    md: 'w-11 h-11 text-lg',
    lg: 'w-16 h-16 text-xl'
  };

  const profilePictureUrl = user?.profilePictureUrl || user?.profilePicture;

  // Debug logging
  console.log('🔍 UserAvatar Debug:', {
    user: user,
    profilePicture: user?.profilePicture,
    profilePictureUrl: user?.profilePictureUrl,
    finalUrl: profilePictureUrl,
    imageError: imageError,
    hasProfilePicture: !!profilePictureUrl
  });

  if (profilePictureUrl && !imageError) {
    return (
      <img
        src={profilePictureUrl}
        alt={user?.name || 'User'}
        className={`${sizeClasses[size]} rounded-full object-cover border-2 border-white shadow-sm ${className}`}
        onError={(e) => {
          console.log('❌ Image failed to load:', profilePictureUrl);
          console.log('❌ Error event:', e);
          setImageError(true);
        }}
        onLoad={() => {
          console.log('✅ Image loaded successfully:', profilePictureUrl);
        }}
      />
    );
  }

  // Fallback to initial
  return (
    <div className={`${sizeClasses[size]} bg-gradient-to-br from-blue-400 to-indigo-500 rounded-full flex items-center justify-center text-white font-bold ${className}`}>
      {user?.name?.charAt(0).toUpperCase() || 'U'}
    </div>
  );
};

const ChatWindow: React.FC<ChatWindowProps> = ({
  conversation,
  currentUser,
  onMessageSent
}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [editingMessageId, setEditingMessageId] = useState<string | null>(null);
  const [editingContent, setEditingContent] = useState('');
  const [updating, setUpdating] = useState(false);
  const [deletingMessageId, setDeletingMessageId] = useState<string | null>(null);
  const [otherParticipantProfile, setOtherParticipantProfile] = useState<User | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const otherParticipant = conversation.participants?.find(p => p && p.id !== currentUser?.id);

  console.log('🔍 ChatWindow Debug Info:');
  console.log('- Conversation ID:', conversation.id);
  console.log('- Current User ID:', currentUser?.id);
  console.log('- Participants:', conversation.participants);
  console.log('- Other Participant:', otherParticipant);

  // Load messages when conversation changes
  useEffect(() => {
    if (conversation && otherParticipant) {
      console.log('🔄 Conversation changed, clearing messages and loading new ones');
      setMessages([]); // Clear previous messages
      loadMessages();
      loadOtherParticipantProfile();
    }
  }, [conversation.id]);

  // Load other participant's full profile data
  const loadOtherParticipantProfile = async () => {
    if (!otherParticipant?.id) {
      console.log('❌ No other participant ID found:', otherParticipant);
      return;
    }

    try {
      setProfileLoading(true);
      console.log(`🔄 Loading profile for user ${otherParticipant.id} (type: ${typeof otherParticipant.id})`);
      
      // Clean the user ID similar to how we do it in sendMessage
      let cleanUserId;
      if (Array.isArray(otherParticipant.id)) {
        cleanUserId = otherParticipant.id[0]?.toString() || '';
      } else if (typeof otherParticipant.id === 'object') {
        cleanUserId = Object.values(otherParticipant.id)[0]?.toString() || '';
      } else {
        cleanUserId = String(otherParticipant.id);
      }
      cleanUserId = cleanUserId.replace(/[^\d]/g, '');
      
      console.log(`🔄 Clean user ID: ${cleanUserId}`);
      
      const profileData = await userService.getUserById(Number(cleanUserId));
      setOtherParticipantProfile(profileData);
      console.log('✅ Loaded profile data:', profileData);
      console.log('🖼️ Profile picture field:', profileData.profilePicture);
      console.log('🖼️ Profile picture URL field:', profileData.profilePictureUrl);
    } catch (error) {
      console.error('❌ Failed to load user profile:', error);
      // Keep the original participant data as fallback
      setOtherParticipantProfile(otherParticipant);
    } finally {
      setProfileLoading(false);
    }
  };

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadMessages = async () => {
    if (!otherParticipant) {
      console.error('No other participant found');
      return;
    }

    try {
      setLoading(true);
      console.log(`🔄 Loading messages with user ${otherParticipant.id} (${otherParticipant.name})`);
      console.log('📋 Current conversation:', conversation);
      console.log('👤 Current user:', currentUser?.id);
      console.log('👥 Other participant:', otherParticipant);
      
      const response = await ChatService.getMessages(otherParticipant.id);
      // Reverse to show oldest first
      setMessages(response.content.reverse());
      console.log(`✅ Loaded ${response.content.length} messages for conversation with ${otherParticipant.name}`);
      console.log('📨 Messages:', response.content);
    } catch (error) {
      console.error('Failed to load messages:', error);
    } finally {
      setLoading(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

    const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newMessage.trim() || sending || !currentUser) return;

    const messageContent = newMessage.trim();
    const recipient = conversation.participants?.find(p => p && p.id !== currentUser.id);

    if (!recipient) {
      console.error("Could not find recipient in conversation");
      console.error("Conversation participants:", conversation.participants);
      console.error("Current user ID:", currentUser.id);
      return;
    }

    const tempId = `temp-${Date.now()}`;
    const tempMessage: Message = {
      id: tempId,
      conversationId: conversation.id,
      senderId: currentUser.id,
      content: messageContent,
      messageType: 'TEXT',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      sender: currentUser,
      isRead: false
    };

    setMessages(prev => [...prev, tempMessage]);
    setNewMessage('');
    
    // Ensure textarea is cleared
    if (textareaRef.current) {
      textareaRef.current.value = '';
    }
    
    setSending(true);

    try {
        console.log("=== DEBUGGING USER IDS ===");
        console.log("Current user object:", JSON.stringify(currentUser, null, 2));
        console.log("Recipient object:", JSON.stringify(recipient, null, 2));
        console.log("Current user ID raw:", currentUser.id);
        console.log("Current user ID type:", typeof currentUser.id);
        console.log("Recipient ID raw:", recipient.id);
        console.log("Recipient ID type:", typeof recipient.id);
        
        // More aggressive cleaning - handle arrays, objects, etc.
        let cleanSenderId;
        let cleanReceiverId;
        
        if (Array.isArray(currentUser.id)) {
            cleanSenderId = currentUser.id[0]?.toString() || '';
        } else if (typeof currentUser.id === 'object') {
            cleanSenderId = Object.values(currentUser.id)[0]?.toString() || '';
        } else {
            cleanSenderId = String(currentUser.id);
        }
        
        if (Array.isArray(recipient.id)) {
            cleanReceiverId = recipient.id[0]?.toString() || '';
        } else if (typeof recipient.id === 'object') {
            cleanReceiverId = Object.values(recipient.id)[0]?.toString() || '';
        } else {
            cleanReceiverId = String(recipient.id);
        }
        
        // Remove any non-numeric characters
        cleanSenderId = cleanSenderId.replace(/[^\d]/g, '');
        cleanReceiverId = cleanReceiverId.replace(/[^\d]/g, '');
        
        console.log("Clean sender ID:", cleanSenderId);
        console.log("Clean receiver ID:", cleanReceiverId);
        console.log("=== END DEBUGGING ===");
        
        if (!cleanSenderId || !cleanReceiverId) {
            throw new Error(`Invalid IDs: senderId=${cleanSenderId}, receiverId=${cleanReceiverId}`);
        }
        
        const sentMessage = await ChatService.sendMessage({
            senderId: cleanSenderId,
            receiverId: cleanReceiverId,
            content: messageContent,
            messageType: 'TEXT',
            replyToId: undefined,
            attachments: []
        });

      setMessages(prev => prev.map(m => m.id === tempId ? sentMessage : m));
      onMessageSent(sentMessage);
    } catch (error) {
      console.error('Failed to send message:', error);
      setMessages(prev => prev.filter(m => m.id !== tempId));
    } finally {
      setSending(false);
    }
  };

  const handleEditMessage = (message: Message) => {
    // Clear any deleting state first
    setDeletingMessageId(null);
    
    // Set editing state
    setEditingMessageId(message.id.toString());
    setEditingContent(message.content);
  };

  const handleCancelEdit = () => {
    setEditingMessageId(null);
    setEditingContent('');
  };

  const handleUpdateMessage = async (messageId: string) => {
    if (!editingContent.trim()) return;

    setUpdating(true);
    try {
      // Get access token from localStorage
      const accessToken = localStorage.getItem('accessToken');
      
      // Call the PUT API to update the message
      const response = await fetch(`/api/direct-messages/${messageId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken && { 'Authorization': `Bearer ${accessToken}` }),
        },
        body: JSON.stringify({
          content: editingContent
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to update message');
      }

      // Update the message locally
      setMessages(prev => prev.map(msg => 
        msg.id === messageId 
          ? { ...msg, content: editingContent }
          : msg
      ));

      // Clear editing state
      setEditingMessageId(null);
      setEditingContent('');
      
    } catch (error) {
      console.error('Error updating message:', error);
      // You might want to show a toast error here
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteMessage = (messageId: string) => {
    setEditingMessageId(null);
    setEditingContent('');
    setDeletingMessageId(messageId);
  };

  const handleConfirmDelete = async () => {
    if (!currentUser || !deletingMessageId) return;

    setUpdating(true);
    try {
      // Get access token from localStorage
      const accessToken = localStorage.getItem('accessToken');
      
      // Use PUT request to update message content to show it was deleted
      const deletedContent = `${currentUser.name} has deleted the message.`;
      
      const response = await fetch(`/api/direct-messages/${deletingMessageId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken && { 'Authorization': `Bearer ${accessToken}` }),
        },
        body: JSON.stringify({
          content: deletedContent
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to delete message');
      }

      // Update the message locally to show deleted state
      setMessages(prev => prev.map(msg => 
        msg.id === deletingMessageId 
          ? { ...msg, content: deletedContent, isDeleted: true }
          : msg
      ));

      // Clear deleting state
      setDeletingMessageId(null);
      
    } catch (error) {
      console.error('Error deleting message:', error);
      // You might want to show a toast error here
    } finally {
      setUpdating(false);
    }
  };

  const handleCancelDelete = () => {
    setDeletingMessageId(null);
  };

  const formatMessageTime = (dateString: string): string => {
    try {
      const date = new Date(dateString);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '';
    }
  };

  const formatMessageDate = (dateString: string): string => {
    try {
      const date = new Date(dateString);
      const today = new Date();
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);

      if (date.toDateString() === today.toDateString()) {
        return 'Today';
      } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
      } else {
        return date.toLocaleDateString();
      }
    } catch {
      return '';
    }
  };

  const shouldShowDateSeparator = (currentMessage: Message, index: number): boolean => {
    if (index === 0) return true;
    
    const previousMessage = messages[index - 1];
    const currentDate = new Date(currentMessage.createdAt).toDateString();
    const previousDate = new Date(previousMessage.createdAt).toDateString();
    
    return currentDate !== previousDate;
  };

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col h-full max-h-[calc(100vh-80px)]">
      {/* Chat Header */}
      <div className="p-4 border-b border-gray-200 bg-white shadow-sm">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="relative mr-3">
              <UserAvatar 
                user={otherParticipantProfile || otherParticipant || null} 
                size="md"
              />
              <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-white" />
            </div>
            <div>
              <h2 className="font-semibold text-gray-800">{(otherParticipantProfile || otherParticipant)?.name || 'Unknown User'}</h2>
              <p className="text-sm text-gray-500">Online</p>
            </div>
          </div>
          
          <button className="p-2 rounded-full hover:bg-gray-100 transition-colors">
            <MoreVertical className="h-5 w-5 text-gray-500" />
          </button>
        </div>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-6 space-y-2 bg-gray-50">
        <AnimatePresence>
          {messages.map((message, index) => {
            const isOwnMessage = message.senderId === currentUser?.id;
            const showDateSeparator = shouldShowDateSeparator(message, index);

            return (
              <React.Fragment key={message.id}>
                {/* Date Separator */}
                {showDateSeparator && (
                  <div className="flex justify-center py-3">
                    <span className="bg-white border border-gray-200 text-gray-500 text-xs px-3 py-1 rounded-full shadow-sm">
                      {formatMessageDate(message.createdAt)}
                    </span>
                  </div>
                )}

                {/* Message */}
                <motion.div
                  initial={{ opacity: 0, y: 15 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -15 }}
                  transition={{ duration: 0.2 }}
                  className={`flex items-end gap-2 ${isOwnMessage ? 'justify-end' : 'justify-start'}`}
                >
                  {!isOwnMessage && (
                    <div className="flex-shrink-0">
                      <UserAvatar 
                        user={otherParticipantProfile || otherParticipant || null} 
                        size="sm"
                      />
                    </div>
                  )}
                  <div className="relative group">
                    <div className={`${editingMessageId === message.id.toString() || deletingMessageId === message.id.toString() ? 'max-w-lg' : 'max-w-md'} px-4 py-2.5 rounded-xl shadow-sm transition-all duration-200 ${
                      message.isDeleted
                        ? 'bg-gray-100 border border-gray-300 text-gray-500'
                        : isOwnMessage
                          ? editingMessageId === message.id.toString() || deletingMessageId === message.id.toString()
                            ? 'bg-blue-50 text-gray-900 border border-blue-200 rounded-br-none'
                            : 'bg-blue-500 text-white rounded-br-none'
                          : editingMessageId === message.id.toString() || deletingMessageId === message.id.toString()
                            ? 'bg-gray-50 text-gray-900 border border-gray-200 rounded-bl-none'
                            : 'bg-white text-gray-800 rounded-bl-none border border-gray-200'
                    }`}>
                      {deletingMessageId === message.id.toString() ? (
                        <div className="space-y-3 min-w-80">
                          <div className="p-4 text-sm rounded-lg border-2 border-red-200 bg-red-50 text-gray-900">
                            <p className="text-red-800 font-medium">Delete this message?</p>
                            <p className="text-gray-600 mt-1">This will change the message to "{currentUser?.name} has deleted the message."</p>
                          </div>
                          <div className="flex justify-end space-x-2">
                            <button
                              onClick={handleCancelDelete}
                              className="flex items-center space-x-1 px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors duration-200"
                              disabled={updating}
                            >
                              <X className="w-3 h-3" />
                              <span>Cancel</span>
                            </button>
                            <button
                              onClick={handleConfirmDelete}
                              className="flex items-center space-x-1 px-3 py-1.5 text-xs font-medium text-white bg-red-500 hover:bg-red-600 disabled:bg-red-300 rounded-md transition-colors duration-200"
                              disabled={updating}
                            >
                              {updating ? (
                                <div className="w-3 h-3 border border-white border-t-transparent rounded-full animate-spin" />
                              ) : (
                                <Trash2 className="w-3 h-3" />
                              )}
                              <span>{updating ? 'Deleting...' : 'Delete'}</span>
                            </button>
                          </div>
                        </div>
                      ) : editingMessageId === message.id.toString() ? (
                        <div className="space-y-3 min-w-80">
                          <div className="relative">
                            <textarea
                              value={editingContent}
                              onChange={(e) => setEditingContent(e.target.value)}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter' && !e.shiftKey) {
                                  e.preventDefault();
                                  handleUpdateMessage(message.id.toString());
                                } else if (e.key === 'Escape') {
                                  e.preventDefault();
                                  handleCancelEdit();
                                }
                              }}
                              className={`w-full p-4 text-sm rounded-lg resize-none border-2 transition-all duration-200 ${
                                isOwnMessage 
                                  ? 'bg-blue-50 border-blue-200 text-gray-900 focus:border-blue-400 focus:bg-white' 
                                  : 'bg-gray-50 border-gray-200 text-gray-900 focus:border-gray-400 focus:bg-white'
                              } focus:outline-none focus:ring-2 focus:ring-blue-500/20`}
                              rows={Math.max(2, Math.ceil(editingContent.length / 50))}
                              placeholder="Edit your message..."
                              autoFocus
                              style={{ minHeight: '80px' }}
                            />
                            <div className="absolute bottom-2 right-2 text-xs text-gray-400">
                              {editingContent.length} chars
                            </div>
                          </div>
                          <div className="flex justify-between items-center">
                            <div className="text-xs text-gray-500">
                              <span className="inline-flex items-center space-x-3">
                                <span>💡 <kbd className="px-1 py-0.5 bg-gray-200 rounded text-xs">Enter</kbd> to save</span>
                                <span><kbd className="px-1 py-0.5 bg-gray-200 rounded text-xs">Esc</kbd> to cancel</span>
                                <span><kbd className="px-1 py-0.5 bg-gray-200 rounded text-xs">Shift+Enter</kbd> for new line</span>
                              </span>
                            </div>
                            <div className="flex space-x-2">
                              <button
                                onClick={handleCancelEdit}
                                className="flex items-center space-x-1 px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors duration-200"
                                disabled={updating}
                              >
                                <X className="w-3 h-3" />
                                <span>Cancel</span>
                              </button>
                              <button
                                onClick={() => handleUpdateMessage(message.id.toString())}
                                className="flex items-center space-x-1 px-3 py-1.5 text-xs font-medium text-white bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 rounded-md transition-colors duration-200"
                                disabled={updating || !editingContent.trim()}
                              >
                                {updating ? (
                                  <div className="w-3 h-3 border border-white border-t-transparent rounded-full animate-spin" />
                                ) : (
                                  <Check className="w-3 h-3" />
                                )}
                                <span>{updating ? 'Saving...' : 'Save'}</span>
                              </button>
                            </div>
                          </div>
                        </div>
                      ) : deletingMessageId === message.id.toString() ? (
                        <div className="space-y-3 min-w-80">
                          <div className="p-4 text-sm rounded-lg border-2 border-red-200 bg-red-50 text-gray-900">
                            <p className="text-red-800 font-medium">Delete this message?</p>
                            <p className="text-gray-600 mt-1">This will change the message to "{currentUser?.name} has deleted the message."</p>
                          </div>
                          <div className="flex justify-end space-x-2">
                            <button
                              onClick={handleCancelDelete}
                              className="flex items-center space-x-1 px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors duration-200"
                              disabled={updating}
                            >
                              <X className="w-3 h-3" />
                              <span>Cancel</span>
                            </button>
                            <button
                              onClick={handleConfirmDelete}
                              className="flex items-center space-x-1 px-3 py-1.5 text-xs font-medium text-white bg-red-500 hover:bg-red-600 disabled:bg-red-300 rounded-md transition-colors duration-200"
                              disabled={updating}
                            >
                              {updating ? (
                                <div className="w-3 h-3 border border-white border-t-transparent rounded-full animate-spin" />
                              ) : (
                                <Trash2 className="w-3 h-3" />
                              )}
                              <span>{updating ? 'Deleting...' : 'Delete'}</span>
                            </button>
                          </div>
                        </div>
                      ) : (
                        <p className={`text-sm ${message.isDeleted ? 'italic text-gray-500' : ''}`}>
                          {message.content}
                        </p>
                      )}
                    </div>
                    
                    {/* Edit and Delete buttons - only show for own messages */}
                    {isOwnMessage && editingMessageId !== message.id.toString() && deletingMessageId !== message.id.toString() && !message.isDeleted && (
                      <div className="absolute -top-2 -right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button
                          onClick={() => handleEditMessage(message)}
                          className="p-1 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors"
                          title="Edit message"
                        >
                          <Edit3 className="w-3 h-3 text-gray-600" />
                        </button>
                        <button
                          onClick={() => handleDeleteMessage(message.id.toString())}
                          className="p-1 bg-gray-100 rounded-full hover:bg-red-100 transition-colors"
                          title="Delete message"
                        >
                          <Trash2 className="w-3 h-3 text-red-500" />
                        </button>
                      </div>
                    )}
                  </div>
                </motion.div>
                <p className={`text-xs text-gray-400 ${isOwnMessage ? 'text-right' : 'text-left ml-10'}`}>
                  {formatMessageTime(message.createdAt)}
                </p>
              </React.Fragment>
            );
          })}
        </AnimatePresence>
        
        <div ref={messagesEndRef} />
      </div>

      {/* Message Input */}
      <div className="p-4 border-t border-gray-200 bg-white shadow-lg">
        <form onSubmit={handleSendMessage} className="flex items-end space-x-3">
          <div className="flex-1 relative">
            <textarea
              ref={textareaRef}
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a message..."
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none min-h-[48px] max-h-32 bg-white text-gray-900 placeholder-gray-500 shadow-sm"
              rows={1}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage(e);
                }
              }}
              disabled={sending}
            />
            
            {/* Action buttons in input */}
            <div className="absolute right-3 bottom-3 flex items-center space-x-1">
              <button
                type="button"
                className="p-2 rounded-full hover:bg-gray-100 transition-colors"
                title="Attach file"
              >
                <Paperclip className="h-5 w-5 text-gray-500" />
              </button>
              <button
                type="button"
                className="p-2 rounded-full hover:bg-gray-100 transition-colors"
                title="Add emoji"
              >
                <Smile className="h-5 w-5 text-gray-500" />
              </button>
            </div>
          </div>
          
          <button
            type="submit"
            disabled={!newMessage.trim() || sending}
            className="p-4 bg-blue-600 text-white rounded-full hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-200"
          >
            <Send className="h-6 w-6" />
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChatWindow;

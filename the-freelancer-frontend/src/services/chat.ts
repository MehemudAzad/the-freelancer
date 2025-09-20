import axios from 'axios';
import { 
  Conversation, 
  ChatConversation,
  Message, 
  User, 
  SendMessageRequest, 
  CreateConversationRequest,
  PaginatedResponse 
} from '@/types/api';
import { getApiUrl } from '@/config/api';

const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api`;

// Get authorization header with custom user headers
const getAuthHeaders = () => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
//   const userString = typeof window !== 'undefined' ? localStorage.getItem('user') : null;
//   const user = userString ? JSON.parse(userString) : null;

  return {
    'Authorization': `Bearer ${token}`,
    // 'Content-Type': 'application/json',
    // 'X-User-Id': user?.id?.toString() || '',
    // 'X-User-Email': user?.email || '',
    // 'X-User-Role': user?.role || ''
  };
};

export class ChatService {
  // Get all users with whom the current user has exchanged messages
  static async getConversations(): Promise<Conversation[]> {
    try {
      console.log('🔄 Fetching chat conversations from:', `${API_BASE_URL}/direct-messages/conversations`);
      console.log('📡 Using headers:', getAuthHeaders());
      
      const response = await axios.get(`${API_BASE_URL}/direct-messages/conversations`, {
        headers: getAuthHeaders()
      });
      
      console.log('✅ Chat conversations response:', response.data);
      
      // Transform the response into Conversation format for compatibility
      const chatConversations = response.data;
      
      if (Array.isArray(chatConversations)) {
        // Convert chat conversations to standard Conversation format
        const conversations: Conversation[] = chatConversations.map((chat: any) => ({
          id: chat.otherUser?.id || chat.userId, // Use other user's ID as conversation ID
          participantIds: [chat.otherUser?.id || chat.userId],
          participants: [chat.otherUser || chat.user],
          lastMessage: chat.lastMessageContent ? {
            id: `last-${chat.otherUser?.id}`,
            conversationId: chat.otherUser?.id,
            senderId: 0, // We don't know who sent it from this endpoint
            content: chat.lastMessageContent,
            messageType: 'TEXT' as const,
            createdAt: chat.lastMessageTime,
            updatedAt: chat.lastMessageTime,
            isRead: true
          } : undefined,
          unreadCount: chat.unreadCount || 0,
          createdAt: chat.lastMessageTime,
          updatedAt: chat.lastMessageTime
        }));
        
        console.log('📱 Transformed conversations:', conversations);
        return conversations;
      }
      
      return response.data;
    } catch (error) {
      console.error('❌ Error fetching conversations:', error);
      if (axios.isAxiosError(error)) {
        console.error('Response data:', error.response?.data);
        console.error('Response status:', error.response?.status);
      }
      throw error;
    }
  }

  // Get recent conversations with message history - this is the correct method for persistent conversations
  static async getRecentConversations(): Promise<Conversation[]> {
    try {
      console.log('🔄 Fetching recent conversations from:', `${API_BASE_URL}/direct-messages/recent`);
      console.log('📡 Using headers:', getAuthHeaders());
      
      const response = await axios.get(`${API_BASE_URL}/direct-messages/recent`, {
        headers: getAuthHeaders()
      });
      
      console.log('✅ Recent conversations response:', response.data);
      
      // Debug: Log the first conversation to see what data we're receiving
      if (response.data.length > 0) {
        console.log('🔍 First conversation data:', response.data[0]);
      }
      
      // The response should be an array of conversation objects based on your API screenshot
      if (Array.isArray(response.data)) {
        return response.data.map((conv: any) => ({
          id: conv.userId || conv.id,
          participantIds: conv.receiverId ? [conv.senderId, conv.receiverId] : [conv.userId],
          participants: [
            {
              id: conv.receiverId || conv.userId,
              name: conv.receiverName || conv.name || 'Unknown User',
              email: conv.email || '',
              handle: conv.handle || null,
              role: conv.receiverRole || conv.role || 'freelancer' as const, // Use actual role from API
              isActive: true,
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString()
            } as User
          ],
          lastMessage: conv.lastMessage ? {
            id: conv.lastMessage.id,
            conversationId: conv.userId,
            senderId: conv.lastMessage.senderId,
            receiverId: conv.lastMessage.receiverId,
            content: conv.lastMessage.content,
            messageType: conv.lastMessage.messageType || 'TEXT',
            createdAt: conv.lastMessage.createdAt,
            updatedAt: conv.lastMessage.updatedAt || conv.lastMessage.createdAt,
            isRead: conv.lastMessage.readAt !== null
          } : undefined,
          unreadCount: conv.unreadCount || 0,
          createdAt: conv.lastMessage?.createdAt || new Date().toISOString(),
          updatedAt: conv.lastMessage?.createdAt || new Date().toISOString()
        } as Conversation));
      }
      
      return [];
    } catch (error) {
      console.error('❌ Error fetching recent conversations:', error);
      if (axios.isAxiosError(error)) {
        console.error('Response data:', error.response?.data);
        console.error('Response status:', error.response?.status);
      }
      throw error;
    }
  }

  // Get messages for a specific conversation with another user
  static async getMessages(otherUserId: number, page: number = 0, size: number = 50): Promise<PaginatedResponse<Message>> {
    try {
      const endpoint = `${API_BASE_URL}/direct-messages/conversations/${otherUserId}`;
      const headers = getAuthHeaders();

      
      console.log(`� Fetching messages with user ${otherUserId}`);
      console.log('🌐 API Endpoint:', endpoint);
      console.log('📡 Request headers:', headers);
      console.log('📄 Request params:', { page, size });
      
      const response = await axios.get(endpoint, {
        headers,
        params: { page, size }
      });
      
      console.log('✅ Raw API response:', response);
      console.log('📨 Messages data:', response.data);
      
      // Handle the actual API response format
      let messagesArray: Message[] = [];
      
      if (response.data && response.data.messages && Array.isArray(response.data.messages)) {
        messagesArray = response.data.messages;
        console.log('📊 Number of messages:', messagesArray.length);
      } else if (response.data && response.data.content && Array.isArray(response.data.content)) {
        // Fallback for paginated format
        messagesArray = response.data.content;
        console.log('📊 Number of messages (paginated):', messagesArray.length);
      } else {
        console.warn('⚠️ Unexpected response format from API');
        messagesArray = [];
      }
      
      // Log individual messages for debugging
      messagesArray.forEach((msg: Message, index: number) => {
        console.log(`📧 Message ${index + 1}:`, {
          id: msg.id,
          senderId: msg.senderId,
          content: msg.content,
          conversationId: msg.conversationId
        });
      });
      
      // Return in expected PaginatedResponse format
      return {
        content: messagesArray,
        totalElements: messagesArray.length,
        totalPages: 1,
        size: messagesArray.length,
        number: 0,
        first: true,
        last: true
      };
    } catch (error) {
      console.error('❌ Error fetching messages:', error);
      if (axios.isAxiosError(error)) {
        console.error('Response data:', error.response?.data);
        console.error('Response status:', error.response?.status);
      }
      throw error;
    }
  }

    // Send a message
  static async sendMessage(request: SendMessageRequest): Promise<Message> {
    try {
      console.log('🚀 Sending message request:', request);
      console.log('📤 SenderId:', request.senderId, '(type:', typeof request.senderId, ')');
      console.log('📥 ReceiverId:', request.receiverId, '(type:', typeof request.receiverId, ')');
      console.log('💬 Content:', request.content);
      console.log('📡 Headers:', getAuthHeaders());
      
      const response = await axios.post(`${API_BASE_URL}/direct-messages`, request, {
        headers: getAuthHeaders()
      });
      
      console.log('✅ Message sent successfully:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ Error sending message:', error);
      if (axios.isAxiosError(error)) {
        console.error('📊 Response data:', error.response?.data);
        console.error('📊 Response status:', error.response?.status);
        console.error('📊 Response headers:', error.response?.headers);
      }
      throw error;
    }
  }

  // Search for users to start conversations with
  static async searchUsers(query: string): Promise<User[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/auth/public/users/search`, {
        // headers: getAuthHeaders(),
        params: { handle: query } // Changed from 'q' to 'handle'
      });
      return response.data.content || response.data;
    } catch (error) {
      console.error('Error searching users:', error);
      throw error;
    }
  }
}

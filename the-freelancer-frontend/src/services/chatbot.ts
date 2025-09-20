import axios from 'axios';

interface ChatbotMessage {
  message: string;
}

interface ChatbotResponse {
  timestamp: string;
  status: string;
  response: string;
}

interface ChatbotSuggestionsResponse {
  timestamp: string;
  suggestions: string[];
  status: string;
}

// Get authentication headers
const getAuthHeaders = () => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('authToken');
    if (token) {
      return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      };
    }
  }
  return {
    'Content-Type': 'application/json',
  };
};

export class ChatbotService {
  /**
   * Test connectivity to the chatbot backend through Next.js API proxy
   */
  static async testConnection(): Promise<boolean> {
    try {
      console.log('🔍 Testing chatbot backend connectivity through API proxy...');
      console.log('🔗 Testing URL:', `/api/chatbot/suggestions`);
      
      const response = await axios.get(`/api/chatbot/suggestions`, {
        headers: getAuthHeaders(),
        timeout: 10000 // 10 second timeout
      });
      
      console.log('✅ Backend connection successful:', response.status);
      return true;
    } catch (error: any) {
      console.error('❌ Backend connection failed:', error.message);
      return false;
    }
  }

  /**
   * Send a message to the chatbot and get AI response through Next.js API proxy
   */
  static async sendMessage(message: string): Promise<ChatbotResponse> {
    try {
      console.log('💬 Sending message to chatbot through API proxy:', message);
      console.log('🔗 POST URL:', `/api/chatbot/chat`);
      console.log('� Headers:', getAuthHeaders());

      const response = await axios.post(`/api/chatbot/chat`, 
        { message },
        {
          headers: getAuthHeaders(),
          timeout: 30000 // 30 second timeout for AI responses
        }
      );

      console.log('✅ Chatbot response received:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error sending message to chatbot:');
      console.error('Request URL:', `/api/chatbot/chat`);
      console.error('Request headers:', getAuthHeaders());
      console.error('Error details:', error.response?.data || error.message);
      console.error('Full error:', error);
      
      throw new Error(error.response?.data?.message || 'Failed to send message to chatbot');
    }
  }

  /**
   * Get conversation starter suggestions from the chatbot through Next.js API proxy
   */
  static async getSuggestions(): Promise<string[]> {
    try {
      console.log('🤖 Fetching chatbot suggestions through API proxy...');
      console.log('🔗 URL:', `/api/chatbot/suggestions`);
      
      const response = await axios.get(`/api/chatbot/suggestions`, {
        headers: getAuthHeaders()
      });
      
      console.log('✅ Chatbot suggestions received:', response.data);
      return response.data.suggestions || [];
    } catch (error: any) {
      console.error('❌ Failed to fetch chatbot suggestions:', error);
      console.error('❌ Error details:', {
        message: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        url: error.config?.url,
        method: error.config?.method
      });
      // Return fallback suggestions if API fails
      return [
        "How do I post a job?",
        "How do I get started as a freelancer?",
        "How do payments work?",
        "What are milestones?",
        "How do I find the right freelancer?"
      ];
    }
  }
}

export default ChatbotService;

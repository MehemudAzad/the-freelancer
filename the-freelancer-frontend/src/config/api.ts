export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  ENDPOINTS: {
    AUTH: '/api/auth',
    USERS: '/api/auth/public/users',
    CONTRACTS: '/api/contracts',
    GIGS: '/api/gigs',
    DIRECT_MESSAGES: '/api/direct-messages',
    AI: '/api/ai',
    JOBS: '/api/jobs',
    CHATBOT: '/api/chatbot',
    PROPOSALS: '/api/proposals',
    INVITES: '/api/invites',
    PAYMENTS: '/api/payments',
    REVIEWS: '/api/reviews',
    TASKS: '/api/tasks',
    WEBSOCKET: '/ws',
    FREELANCERS: '/api/freelancers',
    PROFILES: '/api/profiles',
  }
} as const;

// Helper function to get full URL
export const getApiUrl = (endpoint: string): string => {
  return `${API_CONFIG.BASE_URL}${endpoint}`;
};

// Helper function to get backend URL for server-side requests
export const getBackendUrl = (): string => {
  return process.env.BACKEND_URL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
};

// Configuration validation utility
export const validateConfig = () => {
  const requiredEnvVars = ['NEXT_PUBLIC_API_URL'];
  
  const missing = requiredEnvVars.filter(
    (envVar) => !process.env[envVar]
  );
  
  if (missing.length > 0) {
    console.warn(
      `Missing environment variables: ${missing.join(', ')}. Using defaults.`
    );
  }
  
  if (typeof window === 'undefined') {
    console.log('API Configuration:', {
      BASE_URL: API_CONFIG.BASE_URL,
      BACKEND_URL: getBackendUrl(),
      NODE_ENV: process.env.NODE_ENV,
    });
  }
};

// Initialize configuration validation
validateConfig();
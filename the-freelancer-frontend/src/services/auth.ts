import api from '@/lib/api';
import { LoginRequest, RegisterRequest, LoginResponse, User } from '@/types/api';

export const authAPI = {
  // Register new user
  register: async (userData: RegisterRequest): Promise<LoginResponse> => {
    const response = await api.post('/api/auth/register', userData);
    return response.data;
  },

  // Login user
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post('/api/auth/login', credentials);
    return response.data;
  },

  // Logout user
  logout: async (): Promise<void> => {
    await api.post('/api/auth/logout');
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<LoginResponse> => {
    const response = await api.post('/api/auth/refresh', { refreshToken });
    return response.data;
  },

  // Get user by ID
  getUserById: async (userId: number): Promise<User> => {
    const response = await api.get(`/api/auth/public/users/${userId}`);
    return response.data;
  },
};

export default authAPI;

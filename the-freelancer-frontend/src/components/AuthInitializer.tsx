'use client';

import { useEffect } from 'react';
import { useAppDispatch } from '@/store/hooks';
import { loginSuccess } from '@/store/authSlice';
import { User } from '@/types/api';

export default function AuthInitializer() {
  const dispatch = useAppDispatch();
  
  useEffect(() => {
    // Load auth state from localStorage
    if (typeof window !== 'undefined') {
      const accessToken = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');
      const userString = localStorage.getItem('user');
      
      if (accessToken && refreshToken && userString) {
        try {
          const user = JSON.parse(userString) as User;
          
          // Restore authentication state
          dispatch(loginSuccess({
            accessToken,
            refreshToken,
            user,
          }));
          
          console.log('Auth state restored:', { user: user.name, role: user.role });
        } catch (error) {
          console.error('Failed to parse user data from localStorage', error);
          
          // Clear invalid data
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
        }
      }
    }
  }, [dispatch]);
  
  return null;
}

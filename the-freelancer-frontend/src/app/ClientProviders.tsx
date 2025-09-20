'use client';

import { Provider } from 'react-redux';
import { Toaster } from 'react-hot-toast';
import { usePathname } from 'next/navigation';
import { store } from '@/store';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';
import AuthInitializer from '@/components/AuthInitializer';
import ChatbotButtonWithWidget from '@/components/ChatbotButtonWithWidget';

interface ClientProvidersProps {
  children: React.ReactNode;
}

export function ClientProviders({ children }: ClientProvidersProps) {
  const pathname = usePathname();
  const hideFooter = pathname === '/messages';

  return (
    <Provider store={store}>
      <AuthInitializer />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#fff',
            color: '#333',
            border: '1px solid #e5e7eb',
            borderRadius: '12px',
            boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
          },
          success: {
            duration: 3000,
            iconTheme: {
              primary: '#10b981',
              secondary: '#fff',
            },
          },
          error: {
            duration: 4000,
            iconTheme: {
              primary: '#ef4444',
              secondary: '#fff',
            },
          },
        }}
      />
      <Navbar />
      <main className="flex-1">
        {children}
      </main>
      {!hideFooter && <Footer />}
      
      {/* Global Chatbot - Available on all pages */}
      <ChatbotButtonWithWidget />
    </Provider>
  );
}

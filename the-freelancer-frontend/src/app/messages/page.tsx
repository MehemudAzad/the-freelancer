'use client';
import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { useAppSelector } from '@/store/hooks';
import { ChatService } from '@/services/chat';
import { Conversation, Message, User } from '@/types/api';
import { MessageCircle, Search, Plus, ArrowLeft } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import ConversationList from '@/components/chat/ConversationList';
import ChatWindow from '@/components/chat/ChatWindow';
import UserSearchModal from '@/components/chat/UserSearchModal';

const ChatPage = () => {
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [loading, setLoading] = useState(true);
  const [creatingConversation, setCreatingConversation] = useState(false);
  const [showUserSearch, setShowUserSearch] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Check if mobile view
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // Load conversations on mount
  useEffect(() => {
    if (isAuthenticated) {
      loadConversations();
    }
  }, [isAuthenticated]);

  const loadConversations = async () => {
    try {
      setLoading(true);
      console.log('🔄 Loading conversations from server...');
      
      // Use the recent messages API to get conversations with actual message history
      const data = await ChatService.getRecentConversations();
      console.log('📱 Loaded recent conversations:', data);
      setConversations(data);
    } catch (error) {
      console.error('Failed to load conversations:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleNewConversation = async (recipient: User) => {
    console.log('🚀 Messages Page: Starting new conversation with:', recipient);
    try {
      // Validate recipient
      if (!recipient || !recipient.id) {
        console.error('❌ Invalid recipient:', recipient);
        return;
      }

      // Show loading state for conversation creation
      setCreatingConversation(true);
      
      // Check if conversation already exists
      const existingConversation = conversations.find(conv => 
        conv.participants?.some(p => p?.id === recipient.id)
      );
      
      if (existingConversation) {
        // If conversation already exists, just select it
        setSelectedConversation(existingConversation);
        setShowUserSearch(false);
        console.log(`📱 Opening existing conversation with ${recipient.name} (@${recipient.handle})`);
        return;
      }
      
      // Create local conversation (messages will be created when first message is sent)
      console.log(`🔄 Creating local conversation with ${recipient.name} (@${recipient.handle})...`);
      
      const tempId = Date.now() * -1; // negative temporary id
      const now = new Date().toISOString();
      const conversation: Conversation = {
        id: tempId,
        participantIds: [user!.id, recipient.id],
        participants: [user as User, recipient],
        lastMessage: undefined,
        unreadCount: 0,
        createdAt: now,
        updatedAt: now
      };

      // Update conversations list locally
      setConversations(prev => [conversation, ...prev]);

      // Select the new conversation and close modal
      setSelectedConversation(conversation);
      setShowUserSearch(false);

      console.log(`🎉 Local conversation opened with ${recipient.name} (@${recipient.handle})`);
    } finally {
      setCreatingConversation(false);
    }
  };

  const handleConversationSelect = (conversation: Conversation) => {
    setSelectedConversation(conversation);
  };

  const handleMessageSent = (message: Message) => {
    setConversations(prev =>
      prev.map(conv =>
        conv.id === message.conversationId
          ? { ...conv, lastMessage: message }
          : conv
      )
    );
  };

  const handleBackToList = () => {
    setSelectedConversation(null);
  };

  if (!isAuthenticated) {
    return (
      <div className="h-[calc(100vh-80px)] flex items-center justify-center relative overflow-hidden">
        {/* Background */}
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 via-white to-cyan-50">
          <div className="absolute inset-0 bg-[radial-gradient(circle_800px_at_100%_200px,#d946ef,transparent)] opacity-10"></div>
          <div className="absolute inset-0 bg-[radial-gradient(circle_600px_at_0%_100%,#3b82f6,transparent)] opacity-10"></div>
        </div>

        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, ease: "circOut" }}
          className="relative z-10 text-center p-8 bg-white/80 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/20 max-w-md"
        >
          <motion.div
            animate={{ rotate: [0, 5, -5, 0] }}
            transition={{ duration: 3, repeat: Infinity, ease: "easeInOut" }}
            className="w-20 h-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl mx-auto mb-6 flex items-center justify-center shadow-xl"
          >
            <MessageCircle className="h-10 w-10 text-white" />
          </motion.div>
          <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent mb-4">Please Login 🔐</h2>
          <p className="text-gray-600 text-lg">You need to be logged in to access your messages and start conversations.</p>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-80px)] relative overflow-hidden">
      {/* Animated Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 via-white to-cyan-50">
        <div className="absolute inset-0 bg-[radial-gradient(circle_800px_at_100%_200px,#d946ef,transparent)] opacity-20"></div>
        <div className="absolute inset-0 bg-[radial-gradient(circle_600px_at_0%_100%,#3b82f6,transparent)] opacity-20"></div>
        <div className="absolute inset-0 bg-[radial-gradient(circle_400px_at_50%_0%,#06b6d4,transparent)] opacity-15"></div>
      </div>

      {/* Floating Particles */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-2 h-2 bg-blue-400 rounded-full opacity-30 animate-pulse"></div>
        <div className="absolute top-3/4 right-1/4 w-1 h-1 bg-purple-400 rounded-full opacity-40 animate-ping animation-delay-1000"></div>
        <div className="absolute top-1/2 left-3/4 w-3 h-3 bg-cyan-400 rounded-full opacity-20 animate-pulse animation-delay-2000"></div>
        <div className="absolute top-1/3 right-1/3 w-1.5 h-1.5 bg-indigo-400 rounded-full opacity-35 animate-ping animation-delay-3000"></div>
      </div>

      <div className="relative flex h-full p-6">
        <div className="flex w-full max-w-7xl mx-auto h-full rounded-3xl overflow-hidden shadow-2xl bg-white/80 backdrop-blur-xl border border-white/20">
        {/* Sidebar - Conversation List */}
        <motion.div
          initial={{ x: -300, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ duration: 0.5, ease: "circOut" }}
          className={`${isMobile && selectedConversation ? 'hidden' : 'flex'} flex-col w-full md:w-[380px] lg:w-[420px] relative`}
        >
          {/* Sidebar Glass Background */}
          <div className="absolute inset-0 bg-gradient-to-b from-white/95 via-white/90 to-white/95 backdrop-blur-xl border-r border-white/30"></div>
          <div className="absolute inset-0 bg-gradient-to-br from-blue-50/30 via-transparent to-purple-50/20"></div>
          
          {/* Sidebar Content */}
          <div className="relative z-10 flex flex-col h-full">
          {/* Header */}
          <div className="p-6 border-b border-white/20 bg-gradient-to-r from-white/20 to-transparent">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
                  <MessageCircle className="h-5 w-5 text-white" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">Messages ✨</h1>
                  <p className="text-sm text-gray-500 font-medium">Stay connected with your network</p>
                </div>
              </div>
              <motion.button
                whileHover={{ scale: 1.05, rotate: 90 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setShowUserSearch(true)}
                className="group relative p-3 rounded-2xl bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg hover:shadow-xl transition-all duration-300"
                title="New Message"
              >
                <Plus className="h-5 w-5 transition-transform duration-300" />
                <div className="absolute inset-0 rounded-2xl bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
              </motion.button>
            </div>

            {/* Search Bar */}
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.5 }}
              className="relative group"
            >
              <div className="absolute inset-0 bg-gradient-to-r from-blue-500/20 to-purple-500/20 rounded-2xl blur-lg opacity-0 group-focus-within:opacity-100 transition-opacity duration-300"></div>
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5 transition-colors duration-300 group-focus-within:text-blue-500" />
              <input
                type="text"
                placeholder="🔍 Search conversations..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="relative w-full pl-12 pr-4 py-4 rounded-2xl bg-white/70 backdrop-blur-sm border border-white/30 text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 shadow-lg transition-all duration-300 hover:bg-white/80"
              />
            </motion.div>
          </div>

          {/* Conversations */}
          <div className="flex-1 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-transparent">
            <ConversationList
              conversations={conversations.filter(c =>
                c.participants?.some(p => 
                  p && p.name && (
                    p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                    p.handle?.toLowerCase().includes(searchQuery.toLowerCase())
                  )
                )
              )}
              selectedConversation={selectedConversation}
              onConversationSelect={handleConversationSelect}
              loading={loading}
              currentUser={user}
            />
          </div>
          </div>
        </motion.div>

        {/* Main Chat Area */}
        <div className={`${isMobile && !selectedConversation ? 'hidden' : 'flex'} flex-1 flex flex-col relative`}>
          {/* Chat Area Glass Background */}
          <div className="absolute inset-0 bg-gradient-to-br from-white/60 via-white/70 to-white/80 backdrop-blur-sm"></div>
          
          <div className="relative z-10 flex flex-col h-full">
            <AnimatePresence mode="wait">
              {selectedConversation ? (
                <motion.div
                  key="chat-window"
                  initial={{ opacity: 0, x: 50 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -50 }}
                  transition={{ duration: 0.4, ease: "circOut" }}
                  className="flex flex-col h-full"
                >
                  {/* Mobile Back Button */}
                  {isMobile && (
                    <div className="p-4 border-b border-white/20 bg-gradient-to-r from-white/20 to-transparent md:hidden">
                      <motion.button
                        whileHover={{ x: -5, scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={handleBackToList}
                        className="flex items-center space-x-2 text-blue-600 hover:text-blue-700 transition-colors duration-300"
                      >
                        <ArrowLeft className="h-5 w-5" />
                        <span className="font-medium">Back to conversations</span>
                      </motion.button>
                    </div>
                  )}

                  <ChatWindow
                    conversation={selectedConversation}
                    currentUser={user}
                    onMessageSent={handleMessageSent}
                  />
                </motion.div>
              ) : (
              <motion.div
                key="empty-state"
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.9 }}
                transition={{ duration: 0.5, ease: "circOut" }}
                className="flex-1 flex items-center justify-center relative overflow-hidden"
              >
                {/* Decorative Elements */}
                <div className="absolute inset-0 flex items-center justify-center">
                  <motion.div
                    animate={{ rotate: 360 }}
                    transition={{ duration: 20, repeat: Infinity, ease: "linear" }}
                    className="w-96 h-96 border border-blue-200/30 rounded-full"
                  ></motion.div>
                  <motion.div
                    animate={{ rotate: -360 }}
                    transition={{ duration: 30, repeat: Infinity, ease: "linear" }}
                    className="absolute w-64 h-64 border border-purple-200/20 rounded-full"
                  ></motion.div>
                </div>

                <div className="relative z-10 text-center p-8 max-w-md">
                  {/* Animated Icon */}
                  <motion.div
                    initial={{ scale: 0, rotate: -180 }}
                    animate={{ scale: 1, rotate: 0 }}
                    transition={{ delay: 0.2, duration: 0.6, ease: "circOut" }}
                    className="relative mb-8"
                  >
                    <div className="w-24 h-24 bg-gradient-to-br from-blue-500 via-purple-500 to-cyan-500 rounded-3xl mx-auto flex items-center justify-center shadow-2xl rotate-12 hover:rotate-0 transition-transform duration-500">
                      <MessageCircle className="h-12 w-12 text-white" />
                    </div>
                    <motion.div
                      animate={{ scale: [1, 1.2, 1] }}
                      transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                      className="absolute -inset-4 bg-gradient-to-r from-blue-500/20 to-purple-500/20 rounded-full blur-xl"
                    ></motion.div>
                  </motion.div>

                  {/* Text Content */}
                  <motion.div
                    initial={{ opacity: 0, y: 30 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.4, duration: 0.6 }}
                  >
                    <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-800 via-blue-600 to-purple-600 bg-clip-text text-transparent mb-4">
                      Welcome to Messages ✨
                    </h2>
                    <p className="text-gray-600 text-lg mb-2 font-medium">
                      Your conversations await
                    </p>
                    <p className="text-gray-500 mb-8">
                      Select a conversation to start chatting or create a new one
                    </p>
                  </motion.div>

                  {/* Action Button */}
                  <motion.button
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.6, duration: 0.6 }}
                    whileHover={{ scale: 1.05, y: -2 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => setShowUserSearch(true)}
                    className="group relative px-8 py-4 bg-gradient-to-r from-blue-500 via-purple-500 to-cyan-500 text-white rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 font-semibold text-lg"
                  >
                    <span className="relative z-10 flex items-center space-x-2">
                      <Plus className="h-5 w-5" />
                      <span>Start New Conversation</span>
                    </span>
                    <motion.div
                      className="absolute inset-0 bg-white/20 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                      whileHover={{ scale: 1.05 }}
                    ></motion.div>
                  </motion.button>

                  {/* Floating Elements */}
                  <motion.div
                    animate={{ y: [-10, 10, -10] }}
                    transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
                    className="absolute top-0 right-0 w-6 h-6 bg-blue-400/30 rounded-full blur-sm"
                  ></motion.div>
                  <motion.div
                    animate={{ y: [10, -10, 10] }}
                    transition={{ duration: 3, repeat: Infinity, ease: "easeInOut", delay: 1 }}
                    className="absolute bottom-0 left-0 w-4 h-4 bg-purple-400/30 rounded-full blur-sm"
                  ></motion.div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
          </div>
        </div>
        </div>
      </div>

      {/* User Search Modal */}
      <UserSearchModal
        isOpen={showUserSearch}
        onClose={() => setShowUserSearch(false)}
        onSelectUser={handleNewConversation}
        creatingConversation={creatingConversation}
      />
    </div>
  );
};

export default ChatPage;

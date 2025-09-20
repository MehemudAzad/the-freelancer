import React, { useState, useEffect } from 'react';
import { User } from '@/types/api';
import { ChatService } from '@/services/chat';
import freelancerService from '@/services/freelancer';
import inviteService from '@/services/invite';
import { Search, X, UserPlus, Mail, Briefcase } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface InviteFreelancerModalProps {
  isOpen: boolean;
  onClose: () => void;
  jobId: number;
  jobTitle: string;
  onInviteSent: () => void;
}

const InviteFreelancerModal: React.FC<InviteFreelancerModalProps> = ({
  isOpen,
  onClose,
  jobId,
  jobTitle,
  onInviteSent
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);
  const [sendingInvite, setSendingInvite] = useState(false);
  const [selectedFreelancer, setSelectedFreelancer] = useState<User | null>(null);
  const [message, setMessage] = useState('');
  const [step, setStep] = useState<'search' | 'compose'>('search');
  const [allFreelancers, setAllFreelancers] = useState<User[]>([]);
  const [initialLoading, setInitialLoading] = useState(false);

  // Load initial freelancers when modal opens
  useEffect(() => {
    if (isOpen && allFreelancers.length === 0) {
      loadInitialFreelancers();
    }
  }, [isOpen]);

  // Clear search when modal closes
  useEffect(() => {
    if (!isOpen) {
      setSearchQuery('');
      setSearchResults([]);
      setSelectedFreelancer(null);
      setMessage('');
      setStep('search');
    }
  }, [isOpen]);

  const loadInitialFreelancers = async () => {
    try {
      setInitialLoading(true);
      console.log('🔍 Loading initial freelancers...');
      
      let freelancers: User[] = [];
      
      try {
        freelancers = await freelancerService.getAllFreelancers();
        console.log('✅ API freelancers loaded:', freelancers);
      } catch (error) {
        console.log('❌ API failed, using mock data:', error);
        
        // Fallback mock data for testing
        freelancers = [
          {
            id: 1,
            name: 'John Doe',
            handle: 'johndoe',
            email: 'john@example.com',
            role: 'freelancer' as const,
            isActive: true,
            createdAt: '2024-01-01',
            updatedAt: '2024-01-01'
          },
          {
            id: 2,
            name: 'Jane Smith', 
            handle: 'janesmith',
            email: 'jane@example.com',
            role: 'freelancer' as const,
            isActive: true,
            createdAt: '2024-01-01',
            updatedAt: '2024-01-01'
          },
          {
            id: 3,
            name: 'Alex Johnson',
            handle: 'alexj',
            email: 'alex@example.com', 
            role: 'freelancer' as const,
            isActive: true,
            createdAt: '2024-01-01',
            updatedAt: '2024-01-01'
          }
        ];
        console.log('🔍 Using mock freelancers:', freelancers);
      }
      
      setAllFreelancers(freelancers);
      
      // If no search query, show all freelancers
      if (!searchQuery.trim()) {
        setSearchResults(freelancers);
      }
      
      console.log('✅ Initial freelancers loaded:', freelancers);
    } catch (error) {
      console.error('❌ Failed to load initial freelancers:', error);
    } finally {
      setInitialLoading(false);
    }
  };

  // Debounced search
  useEffect(() => {
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }

    if (searchQuery.trim()) {
      setSearchTimeout(setTimeout(() => {
        handleSearch();
      }, 300));
    } else {
      // Show all freelancers when no search query
      setSearchResults(allFreelancers);
    }

    return () => {
      if (searchTimeout) {
        clearTimeout(searchTimeout);
      }
    };
  }, [searchQuery, allFreelancers]);

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    try {
      setLoading(true);
      console.log('🔍 Searching for freelancers with query:', searchQuery);
      
      // Try the new freelancer service first
      let freelancers: User[] = [];
      
      try {
        freelancers = await freelancerService.searchFreelancers(searchQuery.trim());
        console.log('🔍 Freelancer service results:', freelancers);
      } catch (error) {
        console.log('🔍 Freelancer service failed, trying ChatService...', error);
        
        // Fallback to ChatService
        const results = await ChatService.searchUsers(searchQuery.trim());
        console.log('🔍 ChatService raw results:', results);
        
        freelancers = results.filter(user => {
          console.log('🔍 Checking user:', user.name, 'role:', user.role);
          return user.role === 'freelancer';
        });
        console.log('🔍 ChatService filtered freelancers:', freelancers);
      }
      
      // If no results from search, filter from all freelancers
      if (freelancers.length === 0 && allFreelancers.length > 0) {
        console.log('🔍 No search results, filtering from all freelancers...');
        freelancers = allFreelancers.filter(user => 
          (user.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
           user.handle?.toLowerCase().includes(searchQuery.toLowerCase()) ||
           user.email?.toLowerCase().includes(searchQuery.toLowerCase())) &&
          (user.role === 'freelancer' || (user.role as string) === 'FREELANCER')
        );
        console.log('🔍 Filtered from all freelancers:', freelancers);
      }
      
      setSearchResults(freelancers);
    } catch (error) {
      console.error('❌ Failed to search freelancers:', error);
      setSearchResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFreelancerSelect = (freelancer: User) => {
    console.log('🎯 Selected freelancer:', freelancer);
    setSelectedFreelancer(freelancer);
    setMessage(`Hello ${freelancer.name},\n\nI would like to invite you to work on my project: "${jobTitle}".\n\nPlease review the job details and let me know if you're interested.\n\nBest regards!`);
    setStep('compose');
  };

  const handleSendInvite = async () => {
    if (!selectedFreelancer || !message.trim()) return;

    try {
      setSendingInvite(true);
      
      await inviteService.createInvite({
        jobId,
        freelancerId: selectedFreelancer.id,
        message: message.trim()
      });

      console.log('✅ Invitation sent successfully!');
      onInviteSent();
      onClose();
    } catch (error) {
      console.error('❌ Failed to send invitation:', error);
      // You could add error handling UI here
    } finally {
      setSendingInvite(false);
    }
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget && !sendingInvite) {
      onClose();
    }
  };

  const handleBack = () => {
    setStep('search');
    setSelectedFreelancer(null);
    setMessage('');
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
        onClick={handleBackdropClick}
      >
        <motion.div
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          className="bg-white rounded-2xl shadow-xl w-full max-w-md max-h-[80vh] flex flex-col"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                {step === 'compose' && (
                  <button
                    onClick={handleBack}
                    className="p-1 rounded-full hover:bg-gray-100 transition-colors"
                    disabled={sendingInvite}
                  >
                    <Search className="h-5 w-5 text-gray-500" />
                  </button>
                )}
                <div>
                  <h2 className="text-xl font-bold text-gray-900">
                    {step === 'search' ? 'Invite Freelancer' : 'Send Invitation'}
                  </h2>
                  <p className="text-sm text-gray-500 mt-1">
                    {step === 'search' ? `For: ${jobTitle}` : `To: ${selectedFreelancer?.name}`}
                  </p>
                </div>
              </div>
              <button
                onClick={onClose}
                className="p-2 rounded-full hover:bg-gray-100 transition-colors"
                disabled={sendingInvite}
              >
                <X className="h-5 w-5 text-gray-500" />
              </button>
            </div>

            {step === 'search' && (
              <div className="mt-4 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                <input
                  type="text"
                  placeholder="Search freelancers by name or username..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border text-gray-950 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                  autoFocus
                />
              </div>
            )}
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto">
            {step === 'search' && (
              <>
                {(loading || initialLoading) && (
                  <div className="p-8 text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500 mx-auto mb-4"></div>
                    <p className="text-gray-600">
                      {initialLoading ? 'Loading freelancers...' : 'Searching freelancers...'}
                    </p>
                  </div>
                )}

                {!loading && !initialLoading && searchQuery && searchResults.length === 0 && (
                  <div className="p-8 text-center">
                    <UserPlus className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-gray-600">No freelancers found</p>
                    <p className="text-sm text-gray-400 mt-1">Try searching with a different term</p>
                  </div>
                )}

                {!loading && !initialLoading && !searchQuery && searchResults.length === 0 && (
                  <div className="p-8 text-center">
                    <Search className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-gray-600">Search for freelancers</p>
                    <p className="text-sm text-gray-400 mt-1">Type a name or username to find freelancers</p>
                  </div>
                )}

                {!loading && !initialLoading && !searchQuery && searchResults.length > 0 && (
                  <div className="p-4 text-center border-b border-gray-200">
                    <p className="text-sm text-gray-500">
                      Showing {searchResults.length} available freelancers
                    </p>
                  </div>
                )}

                {!loading && searchResults.length > 0 && (
                  <div className="p-2">
                    {searchResults.map((freelancer) => (
                      <motion.div
                        key={freelancer.id}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        whileHover={{ backgroundColor: 'rgba(147, 51, 234, 0.05)' }}
                        className="flex items-center p-4 rounded-lg transition-colors cursor-pointer hover:bg-purple-50"
                        onClick={() => handleFreelancerSelect(freelancer)}
                      >
                        {/* Avatar */}
                        <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-600 rounded-full flex items-center justify-center text-white font-semibold mr-4">
                          {freelancer.name?.charAt(0).toUpperCase() || 'F'}
                        </div>

                        {/* User Info */}
                        <div className="flex-1 min-w-0">
                          <h3 className="font-medium text-gray-900 truncate">{freelancer.name || 'Unknown Freelancer'}</h3>
                          <p className="text-sm text-gray-500 truncate">
                            @{freelancer.handle || 'unknown'} • {freelancer.email || 'no email'}
                          </p>
                          
                          {/* Role Badge */}
                          <div className="mt-1">
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">
                              {((freelancer.role as string) === 'FREELANCER' ? 'FREELANCER' : 'Freelancer').toUpperCase()}
                            </span>
                          </div>
                        </div>

                        {/* Action Indicator */}
                        <div className="ml-4">
                          <div className="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
                            <Mail className="h-4 w-4 text-purple-600" />
                          </div>
                        </div>
                      </motion.div>
                    ))}
                  </div>
                )}
              </>
            )}

            {step === 'compose' && selectedFreelancer && (
              <div className="p-6">
                {/* Selected Freelancer Info */}
                <div className="flex items-center p-4 bg-purple-50 rounded-lg mb-6">
                  <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-600 rounded-full flex items-center justify-center text-white font-semibold mr-4">
                    {selectedFreelancer.name?.charAt(0).toUpperCase() || 'F'}
                  </div>
                  <div className="flex-1">
                    <h3 className="font-medium text-gray-900">{selectedFreelancer.name}</h3>
                    <p className="text-sm text-gray-500">@{selectedFreelancer.handle}</p>
                  </div>
                  <Briefcase className="h-5 w-5 text-purple-600" />
                </div>

                {/* Job Info */}
                <div className="mb-6">
                  <h4 className="font-medium text-gray-900 mb-2">Job:</h4>
                  <p className="text-gray-700 bg-gray-50 p-3 rounded-lg">{jobTitle}</p>
                </div>

                {/* Message Input */}
                <div className="mb-6">
                  <label className="block font-medium text-gray-900 mb-2">Invitation Message:</label>
                  <textarea
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Write a personal message to the freelancer..."
                    rows={6}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none text-gray-900 bg-white placeholder-gray-400"
                    disabled={sendingInvite}
                    style={{ color: '#1f2937', backgroundColor: '#ffffff' }}
                  />
                  <p className="text-xs text-gray-500 mt-1">{message.length}/500 characters</p>
                </div>

                {/* Send Button */}
                <button
                  onClick={handleSendInvite}
                  disabled={!message.trim() || sendingInvite}
                  className="w-full bg-gradient-to-r from-purple-500 to-pink-600 text-white py-3 px-6 rounded-lg font-medium hover:from-purple-600 hover:to-pink-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {sendingInvite ? (
                    <>
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                      <span>Sending Invitation...</span>
                    </>
                  ) : (
                    <>
                      <Mail className="h-5 w-5" />
                      <span>Send Invitation</span>
                    </>
                  )}
                </button>
              </div>
            )}
          </div>

          {/* Footer */}
          {step === 'search' && (
            <div className="p-4 border-t border-gray-200">
              <p className="text-sm text-gray-500 text-center">
                Select a freelancer to send them a job invitation
              </p>
            </div>
          )}
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
};

export default InviteFreelancerModal;
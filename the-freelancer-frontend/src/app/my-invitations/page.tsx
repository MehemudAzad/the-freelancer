'use client';

import React, { useState, useEffect } from 'react';
import { useAppSelector } from '@/store/hooks';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Mail, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Briefcase, 
  User,
  Calendar,
  DollarSign,
  MapPin,
  ArrowLeft
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import inviteService from '@/services/invite';

interface Invitation {
  id: number;
  jobId: number;
  jobTitle: string;
  clientId: number;
  clientName: string;
  freelancerId: number;
  freelancerName: string;
  status: 'SENT' | 'ACCEPTED' | 'DECLINED';
  message?: string;
  createdAt: string;
  updatedAt: string;
}

const MyInvitationsPage = () => {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [invitations, setInvitations] = useState<Invitation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingId, setProcessingId] = useState<number | null>(null);

  useEffect(() => {
    // if (!isAuthenticated) {
    //   router.push('/auth/login');
    //   return;
    // }

    if (user?.role !== 'FREELANCER') {
      router.push('/dashboard');
      return;
    }

    fetchInvitations();
  }, [user, isAuthenticated, router]);

  const fetchInvitations = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await inviteService.getMyReceivedInvites();
      console.log('📨 Received invitations:', response);
      
      setInvitations(response);
    } catch (err: any) {
      console.error('❌ Failed to fetch invitations:', err);
      setError(err.response?.data?.message || 'Failed to load invitations');
    } finally {
      setLoading(false);
    }
  };

  const handleInvitationAction = async (invitationId: number, status: 'ACCEPTED' | 'DECLINED') => {
    try {
      setProcessingId(invitationId);
      
      await inviteService.updateInviteStatus(invitationId, status);
      
      // Update local state
      setInvitations(prev => 
        prev.map(invite => 
          invite.id === invitationId 
            ? { ...invite, status: status }
            : invite
        )
      );
      
      console.log(`✅ Invitation ${status.toLowerCase()} successfully`);
    } catch (err: any) {
      console.error(`❌ Failed to ${status.toLowerCase()} invitation:`, err);
      // You could show a toast notification here
    } finally {
      setProcessingId(null);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SENT':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'ACCEPTED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'DECLINED':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SENT':
        return <Clock className="h-4 w-4" />;
      case 'ACCEPTED':
        return <CheckCircle className="h-4 w-4" />;
      case 'DECLINED':
        return <XCircle className="h-4 w-4" />;
      default:
        return <Mail className="h-4 w-4" />;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 flex items-center justify-center">
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center"
        >
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto mb-4"></div>
          <p className="text-gray-600 text-lg">Loading your invitations...</p>
        </motion.div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 flex items-center justify-center px-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center max-w-md"
        >
          <div className="bg-red-50 border border-red-200 rounded-2xl p-8">
            <XCircle className="h-16 w-16 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-red-800 mb-2">Error Loading Invitations</h2>
            <p className="text-red-600 mb-4">{error}</p>
            <button
              onClick={fetchInvitations}
              className="bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors"
            >
              Try Again
            </button>
          </div>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4 py-8">
      <div className="max-w-4xl mx-auto">
        
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-8"
        >
          <div className="flex items-center justify-between mb-6">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => router.push('/dashboard')}
              className="flex items-center gap-2 text-gray-600 hover:text-gray-800 transition-colors"
            >
              <ArrowLeft className="h-5 w-5" />
              Back to Dashboard
            </motion.button>
          </div>
          
          <div className="text-center">
            <motion.h1
              initial={{ scale: 0.9, opacity: 0.8 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ duration: 0.8, type: 'spring' }}
              className="text-4xl font-extrabold bg-gradient-to-r from-purple-600 via-blue-600 to-cyan-600 bg-clip-text text-transparent mb-2"
            >
              My Job Invitations
            </motion.h1>
            <p className="text-gray-600 text-lg">
              Manage invitations from clients who want to work with you
            </p>
          </div>
        </motion.div>

        {/* Invitations List */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          {invitations.length === 0 ? (
            <div className="text-center py-16">
              <Mail className="h-24 w-24 text-gray-300 mx-auto mb-6" />
              <h3 className="text-2xl font-bold text-gray-700 mb-2">No Invitations Yet</h3>
              <p className="text-gray-500 text-lg">
                When clients invite you to their projects, they'll appear here
              </p>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => router.push('/jobs')}
                className="mt-6 bg-gradient-to-r from-purple-500 to-blue-600 text-white px-8 py-4 rounded-xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300"
              >
                Browse Jobs
              </motion.button>
            </div>
          ) : (
            <div className="space-y-6">
              {invitations.map((invitation, index) => (
                <motion.div
                  key={invitation.id}
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: index * 0.1 }}
                  className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl overflow-hidden hover:shadow-2xl transition-all duration-300"
                >
                  {/* Invitation Header */}
                  <div className="p-6 border-b border-gray-100">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-3">
                          <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                            {invitation.clientName?.charAt(0).toUpperCase() || 'C'}
                          </div>
                          <div>
                            <h3 className="font-bold text-xl text-gray-900">{invitation.jobTitle}</h3>
                            <p className="text-purple-600 font-medium">from {invitation.clientName}</p>
                          </div>
                        </div>
                        
                        <div className="flex items-center gap-4 text-sm text-gray-500 mb-4">
                          <span className="flex items-center gap-1">
                            <Calendar className="h-4 w-4" />
                            {new Date(invitation.createdAt).toLocaleDateString()}
                          </span>
                          <span className="flex items-center gap-1">
                            <Briefcase className="h-4 w-4" />
                            Job ID: {invitation.jobId}
                          </span>
                        </div>
                      </div>
                      
                      <div className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(invitation.status)} flex items-center gap-1`}>
                        {getStatusIcon(invitation.status)}
                        {invitation.status}
                      </div>
                    </div>
                  </div>

                  {/* Message */}
                  {invitation.message && (
                    <div className="px-6 py-4 bg-gray-50/50">
                      <h4 className="font-medium text-gray-900 mb-2">Client Message:</h4>
                      <p className="text-gray-700 leading-relaxed">{invitation.message}</p>
                    </div>
                  )}

                  {/* Action Buttons */}
                  {invitation.status === 'SENT' && (
                    <div className="px-6 py-4 bg-gradient-to-r from-gray-50 to-white">
                      <div className="flex gap-3 justify-end">
                        <motion.button
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          onClick={() => handleInvitationAction(invitation.id, 'DECLINED')}
                          disabled={processingId === invitation.id}
                          className="flex items-center gap-2 px-6 py-3 bg-gray-500 text-white rounded-xl hover:bg-gray-600 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {processingId === invitation.id ? (
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                          ) : (
                            <XCircle className="h-4 w-4" />
                          )}
                          Decline
                        </motion.button>
                        
                        <motion.button
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          onClick={() => handleInvitationAction(invitation.id, 'ACCEPTED')}
                          disabled={processingId === invitation.id}
                          className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-xl hover:from-green-600 hover:to-emerald-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {processingId === invitation.id ? (
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                          ) : (
                            <CheckCircle className="h-4 w-4" />
                          )}
                          Accept
                        </motion.button>
                      </div>
                    </div>
                  )}

                  {/* Already Processed */}
                  {invitation.status !== 'SENT' && (
                    <div className="px-6 py-4 bg-gradient-to-r from-gray-50 to-white">
                      <div className="flex items-center justify-center gap-2 text-gray-600">
                        {getStatusIcon(invitation.status)}
                        <span className="font-medium">
                          You {invitation.status.toLowerCase()} this invitation on{' '}
                          {new Date(invitation.updatedAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  )}
                </motion.div>
              ))}
            </div>
          )}
        </motion.div>

        {/* Quick Actions */}
        {invitations.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="mt-8 pt-6 border-t border-gray-200"
          >
            <div className="flex justify-center gap-4">
              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={fetchInvitations}
                className="bg-purple-500 text-white px-6 py-3 rounded-xl font-medium hover:bg-purple-600 transition-all duration-300"
              >
                Refresh Invitations
              </motion.button>
              
              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => router.push('/jobs')}
                className="bg-blue-500 text-white px-6 py-3 rounded-xl font-medium hover:bg-blue-600 transition-all duration-300"
              >
                Browse More Jobs
              </motion.button>
            </div>
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default MyInvitationsPage;
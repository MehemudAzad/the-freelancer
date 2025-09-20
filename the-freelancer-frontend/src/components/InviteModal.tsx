'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Send, User, Briefcase, AlertCircle, CheckCircle } from 'lucide-react';
import inviteService, { InviteRequest } from '@/services/invite';
import { useAppSelector } from '@/store/hooks';

interface InviteModalProps {
  isOpen: boolean;
  onClose: () => void;
  jobId: number;
  jobTitle: string;
  freelancerId: number;
  freelancerName: string;
  freelancerHandle?: string;
  onInviteSent?: () => void;
}

const InviteModal: React.FC<InviteModalProps> = ({
  isOpen,
  onClose,
  jobId,
  jobTitle,
  freelancerId,
  freelancerName,
  freelancerHandle,
  onInviteSent
}) => {
  const { user } = useAppSelector((state) => state.auth);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async () => {
    if (!user) {
      setError('You must be logged in to send invitations');
      return;
    }

    if (user.role !== 'client') {
      setError('Only clients can send job invitations');
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);

      const inviteData: InviteRequest = {
        jobId: jobId,
        clientId: user.id,
        freelancerId: freelancerId
      };

      await inviteService.createInvite(inviteData);
      
      setSuccess(true);
      setTimeout(() => {
        onInviteSent?.();
        onClose();
        setSuccess(false);
      }, 2000);

    } catch (err: any) {
      console.error('Error sending invitation:', err);
      setError(err.response?.data?.message || 'Failed to send invitation. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setError(null);
      setSuccess(false);
      onClose();
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="absolute inset-0 bg-black/50 backdrop-blur-sm"
            onClick={handleClose}
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="relative bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4 z-10"
          >
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                <Send className="h-6 w-6 text-blue-600" />
                Send Invitation
              </h2>
              <button
                onClick={handleClose}
                disabled={isSubmitting}
                className="p-2 hover:bg-gray-100 rounded-full transition-colors disabled:opacity-50"
              >
                <X className="h-5 w-5 text-gray-500" />
              </button>
            </div>

            {success ? (
              /* Success State */
              <div className="text-center py-8">
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-4"
                >
                  <CheckCircle className="h-8 w-8 text-green-600" />
                </motion.div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  Invitation Sent!
                </h3>
                <p className="text-gray-600">
                  Your invitation has been sent to {freelancerName}. They will be notified and can accept or decline the invitation.
                </p>
              </div>
            ) : (
              /* Main Content */
              <>
                {/* Job Info */}
                <div className="bg-blue-50 rounded-xl p-4 mb-6">
                  <div className="flex items-start gap-3">
                    <Briefcase className="h-5 w-5 text-blue-600 mt-1" />
                    <div>
                      <h3 className="font-semibold text-blue-900 mb-1">Job</h3>
                      <p className="text-blue-800 text-sm">{jobTitle}</p>
                    </div>
                  </div>
                </div>

                {/* Freelancer Info */}
                <div className="bg-purple-50 rounded-xl p-4 mb-6">
                  <div className="flex items-start gap-3">
                    <User className="h-5 w-5 text-purple-600 mt-1" />
                    <div>
                      <h3 className="font-semibold text-purple-900 mb-1">Freelancer</h3>
                      <p className="text-purple-800 text-sm">{freelancerName}</p>
                      {freelancerHandle && (
                        <p className="text-purple-600 text-xs">@{freelancerHandle}</p>
                      )}
                    </div>
                  </div>
                </div>

                {/* Description */}
                <div className="mb-6">
                  <p className="text-gray-600 text-sm leading-relaxed">
                    You're about to invite <span className="font-medium">{freelancerName}</span> to apply for your job 
                    "<span className="font-medium">{jobTitle}</span>". The freelancer will receive a notification 
                    and can choose to accept or decline the invitation.
                  </p>
                </div>

                {/* Error Message */}
                {error && (
                  <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-xl">
                    <div className="flex items-center gap-2">
                      <AlertCircle className="h-4 w-4 text-red-600" />
                      <p className="text-red-800 text-sm">{error}</p>
                    </div>
                  </div>
                )}

                {/* Actions */}
                <div className="flex gap-3 pt-4">
                  <button
                    onClick={handleClose}
                    disabled={isSubmitting}
                    className="flex-1 px-4 py-3 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors disabled:opacity-50 font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSubmit}
                    disabled={isSubmitting}
                    className="flex-1 px-4 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50 font-medium flex items-center justify-center gap-2"
                  >
                    {isSubmitting ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        Sending...
                      </>
                    ) : (
                      <>
                        <Send className="h-4 w-4" />
                        Send Invitation
                      </>
                    )}
                  </button>
                </div>
              </>
            )}
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default InviteModal;
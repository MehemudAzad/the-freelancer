'use client';

import React, { useState, useEffect } from 'react';
import { useAppSelector } from '@/store/hooks';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  FileText, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Briefcase, 
  User,
  Calendar,
  DollarSign,
  MapPin,
  ArrowLeft,
  MessageSquare,
  Building,
  Activity
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';

interface Contract {
  id: number;
  contractId: number;
  jobTitle: string;
  clientId: number;
  freelancerId: number;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'PENDING';
  createdAt: string;
  updatedAt: string;
  messageCount: number;
  fileCount: number;
  taskCount: number;
  eventCount: number;
}

const MyContractsPage = () => {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // if (!isAuthenticated) {
    //   router.push('/auth/login');
    //   return;
    // }

    if (user?.role !== 'FREELANCER') {
      router.push('/dashboard');
      return;
    }

    fetchContracts();
  }, [user, isAuthenticated, router]);

  const fetchContracts = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await api.get('/api/workspaces/rooms/my-rooms');
      console.log('📋 Received contracts:', response.data);
      
      setContracts(response.data);
    } catch (err: any) {
      console.error('❌ Failed to fetch contracts:', err);
      setError(err.response?.data?.message || 'Failed to load contracts');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'COMPLETED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Activity className="h-4 w-4" />;
      case 'COMPLETED':
        return <CheckCircle className="h-4 w-4" />;
      case 'CANCELLED':
        return <XCircle className="h-4 w-4" />;
      case 'PENDING':
        return <Clock className="h-4 w-4" />;
      default:
        return <FileText className="h-4 w-4" />;
    }
  };

  const handleContractClick = (contractId: number) => {
    console.log('➡️ Navigating to contract workspace:', contractId);
    router.push(`/workspace/${contractId}`);
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
          <p className="text-gray-600 text-lg">Loading your contracts...</p>
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
            <h2 className="text-xl font-bold text-red-800 mb-2">Error Loading Contracts</h2>
            <p className="text-red-600 mb-4">{error}</p>
            <button
              onClick={fetchContracts}
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
      <div className="max-w-6xl mx-auto">
        
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
              My Contracts
            </motion.h1>
            <p className="text-gray-600 text-lg">
              Manage your active projects and workspaces
            </p>
          </div>
        </motion.div>

        {/* Stats Cards */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8"
        >
          {[
            { label: 'Total Contracts', value: contracts.length, color: 'from-blue-500 to-cyan-500', icon: FileText },
            { label: 'Active', value: contracts.filter(c => c.status === 'ACTIVE').length, color: 'from-green-500 to-emerald-500', icon: Activity },
            { label: 'Completed', value: contracts.filter(c => c.status === 'LOCKED').length, color: 'from-purple-500 to-indigo-500', icon: CheckCircle },
            { label: 'Total Messages', value: contracts.reduce((sum, c) => sum + (c.messageCount || 0), 0), color: 'from-orange-500 to-red-500', icon: MessageSquare }
          ].map((stat, index) => (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 + index * 0.1 }}
              className="bg-white/90 backdrop-blur-md rounded-2xl p-6 shadow-lg hover:shadow-xl transition-all duration-300"
            >
              <div className={`w-12 h-12 rounded-xl bg-gradient-to-r ${stat.color} flex items-center justify-center mb-4`}>
                <stat.icon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-1">{stat.value}</h3>
              <p className="text-gray-600 text-sm">{stat.label}</p>
            </motion.div>
          ))}
        </motion.div>

        {/* Contracts List */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
        >
          {contracts.length === 0 ? (
            <div className="text-center py-16">
              <FileText className="h-24 w-24 text-gray-300 mx-auto mb-6" />
              <h3 className="text-2xl font-bold text-gray-700 mb-2">No Contracts Yet</h3>
              <p className="text-gray-500 text-lg mb-6">
                When you get hired for projects, your contracts will appear here
              </p>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => router.push('/jobs')}
                className="bg-gradient-to-r from-purple-500 to-blue-600 text-white px-8 py-4 rounded-xl font-semibold shadow-lg hover:shadow-xl transition-all duration-300"
              >
                Browse Jobs
              </motion.button>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {contracts.map((contract, index) => (
                <motion.div
                  key={contract.id}
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: index * 0.1 }}
                  className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl overflow-hidden hover:shadow-2xl transition-all duration-300 cursor-pointer group"
                  onClick={() => handleContractClick(contract.contractId)}
                >
                  {/* Contract Header */}
                  <div className="p-6 border-b border-gray-100">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                            <Building className="h-6 w-6" />
                          </div>
                          <div>
                            <h3 className="font-bold text-xl text-gray-900 group-hover:text-purple-600 transition-colors">
                              {contract.jobTitle}
                            </h3>
                            <p className="text-gray-500 text-sm">Contract #{contract.contractId}</p>
                          </div>
                        </div>
                      </div>
                      
                      <div className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(contract.status)} flex items-center gap-1`}>
                        {getStatusIcon(contract.status)}
                        {contract.status}
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-4 text-sm text-gray-500">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-4 w-4" />
                        Started {new Date(contract.createdAt).toLocaleDateString()}
                      </span>
                      {contract.status === 'LOCKED' && (
                        <span className="flex items-center gap-1">
                          <CheckCircle className="h-4 w-4 text-green-500" />
                          Completed {new Date(contract.updatedAt).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Contract Stats */}
                  <div className="p-6 bg-gradient-to-r from-gray-50 to-white">
                    <div className="grid grid-cols-4 gap-4">
                      <div className="text-center">
                        <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                          <MessageSquare className="h-5 w-5 text-blue-600" />
                        </div>
                        <p className="text-lg font-bold text-gray-900">{contract.messageCount || 0}</p>
                        <p className="text-xs text-gray-500">Messages</p>
                      </div>
                      
                      <div className="text-center">
                        <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                          <FileText className="h-5 w-5 text-green-600" />
                        </div>
                        <p className="text-lg font-bold text-gray-900">{contract.fileCount || 0}</p>
                        <p className="text-xs text-gray-500">Files</p>
                      </div>
                      
                      <div className="text-center">
                        <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                          <CheckCircle className="h-5 w-5 text-purple-600" />
                        </div>
                        <p className="text-lg font-bold text-gray-900">{contract.taskCount || 0}</p>
                        <p className="text-xs text-gray-500">Tasks</p>
                      </div>
                      
                      <div className="text-center">
                        <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center mx-auto mb-2">
                          <Calendar className="h-5 w-5 text-orange-600" />
                        </div>
                        <p className="text-lg font-bold text-gray-900">{contract.eventCount || 0}</p>
                        <p className="text-xs text-gray-500">Events</p>
                      </div>
                    </div>
                  </div>

                  {/* Action Indicator */}
                  <div className="px-6 py-3 bg-gradient-to-r from-purple-50 to-blue-50 border-t border-gray-100">
                    <div className="flex items-center justify-center text-purple-600 group-hover:text-purple-700 transition-colors">
                      <span className="text-sm font-medium">Click to open workspace</span>
                      <ArrowLeft className="h-4 w-4 ml-2 transform rotate-180 group-hover:translate-x-1 transition-transform" />
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          )}
        </motion.div>

        {/* Quick Actions */}
        {contracts.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.5 }}
            className="mt-8 pt-6 border-t border-gray-200"
          >
            <div className="flex justify-center gap-4">
              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={fetchContracts}
                className="bg-purple-500 text-white px-6 py-3 rounded-xl font-medium hover:bg-purple-600 transition-all duration-300"
              >
                Refresh Contracts
              </motion.button>
              
              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => router.push('/jobs')}
                className="bg-blue-500 text-white px-6 py-3 rounded-xl font-medium hover:bg-blue-600 transition-all duration-300"
              >
                Find More Work
              </motion.button>
            </div>
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default MyContractsPage;

'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/store/hooks';
import { Button } from '@/components/ui/button';
import jobService from '@/services/job';
import { Job } from '@/types/api';
import { Plus, Edit, Trash2, AlertCircle, CheckCircle, Clock, Search } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { motion, AnimatePresence } from 'framer-motion';

export default function ManageJobsPage() {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; jobId: number | null }>({
    show: false,
    jobId: null
  });
  const [deleteStatus, setDeleteStatus] = useState<{ success: boolean; message: string; show: boolean }>({
    success: false,
    message: '',
    show: false
  });

  useEffect(() => {
    if (isAuthenticated && user?.id) fetchMyJobs();
  }, [isAuthenticated, user]);

  const fetchMyJobs = async () => {
    try {
      setLoading(true);
      const response = await jobService.jobAPI.getMyJobs();
      setJobs(response || []);
    } catch (error) {
      console.error('Error fetching jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteJob = async (jobId: number) => {
    try {
      await jobService.jobAPI.deleteJob(jobId);
      setDeleteStatus({ success: true, message: 'Job deleted successfully', show: true });
      setJobs(jobs.filter(job => job.id !== jobId));
      setTimeout(() => setDeleteStatus(prev => ({ ...prev, show: false })), 3000);
    } catch {
      setDeleteStatus({ success: false, message: 'Failed to delete job', show: true });
      setTimeout(() => setDeleteStatus(prev => ({ ...prev, show: false })), 3000);
    } finally {
      setDeleteModal({ show: false, jobId: null });
    }
  };

  const handleEditJob = (jobId: number) => router.push(`/dashboard/jobs/edit/${jobId}`);

  const formatDate = (dateString: string | Date) =>
    dateString ? formatDistanceToNow(new Date(dateString), { addSuffix: true }) : 'N/A';

  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      open: 'bg-green-100 text-green-800',
      in_progress: 'bg-blue-100 text-blue-800',
      completed: 'bg-gray-100 text-gray-800',
      cancelled: 'bg-red-100 text-red-800',
    };
    return <span className={`px-3 py-1 rounded-full text-xs font-semibold ${styles[status.toLowerCase()] || 'bg-gray-100 text-gray-800'}`}>{status.replace('_', ' ')}</span>;
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white p-8 rounded-2xl shadow-lg text-center max-w-md w-full"
        >
          <AlertCircle className="h-12 w-12 text-yellow-500 mx-auto mb-4" />
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Authentication Required</h1>
          <p className="text-gray-600 mb-6">You need to log in to view this page.</p>
          <Button onClick={() => router.push('/auth/login')} className="bg-blue-600 hover:bg-blue-700 text-white">
            Log In
          </Button>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-6xl mx-auto px-6">
        {/* Page Header */}
        <div className="flex justify-between items-center mb-10">
          <div>
            <h1 className="text-4xl font-extrabold text-gray-900">Manage Your Jobs</h1>
            <p className="text-gray-600 mt-2">Keep track of all your job postings with ease.</p>
          </div>
          <Button onClick={() => router.push('/jobs/post')} className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg shadow">
            <Plus className="mr-2 h-5 w-5" /> Post New Job
          </Button>
        </div>

        {/* Success/Error Alerts */}
        <AnimatePresence>
          {deleteStatus.show && (
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className={`mb-6 p-4 rounded-lg shadow ${deleteStatus.success ? 'bg-green-50 border-l-4 border-green-500' : 'bg-red-50 border-l-4 border-red-500'}`}
            >
              <div className="flex items-center">
                {deleteStatus.success ? <CheckCircle className="h-5 w-5 text-green-500 mr-2" /> : <AlertCircle className="h-5 w-5 text-red-500 mr-2" />}
                <p className={deleteStatus.success ? 'text-green-700' : 'text-red-700'}>{deleteStatus.message}</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Loading Skeleton */}
        {loading ? (
          <div className="bg-white p-6 rounded-2xl shadow animate-pulse space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-12 bg-gray-200 rounded-lg"></div>
            ))}
          </div>
        ) : jobs.length === 0 ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="bg-white text-center p-10 rounded-2xl shadow"
          >
            <Search className="h-16 w-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No Jobs Yet</h3>
            <p className="text-gray-500 mb-6">Start by posting your first job today.</p>
            <Button onClick={() => router.push('/jobs/post')} className="bg-blue-600 hover:bg-blue-700 text-white">
              Post Your First Job
            </Button>
          </motion.div>
        ) : (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="bg-white rounded-2xl shadow overflow-hidden">
            <table className="min-w-full text-sm text-left">
              <thead className="bg-gray-100">
                <tr>
                  {['Job Title', 'Status', 'Posted', 'Proposals', 'Actions'].map((h) => (
                    <th key={h} className="px-6 py-4 font-semibold text-gray-600 uppercase text-xs">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {jobs.map((job) => (
                  <motion.tr
                    key={job.id}
                    whileHover={{ backgroundColor: '#f9fafb' }}
                    className="border-t"
                  >
                    <td className="px-6 py-4 font-medium text-blue-600 cursor-pointer" onClick={() => router.push(`/jobs/${job.id}`)}>
                      {job.title}
                      <div className="text-xs text-gray-500">{job.description.substring(0, 60)}...</div>
                    </td>
                    <td className="px-6 py-4">{getStatusBadge(job.status)}</td>
                    <td className="px-6 py-4 text-gray-500">{formatDate(job.createdAt || job.created_at)}</td>
                    <td className="px-6 py-4 text-gray-500">{job.proposalCount || 0}</td>
                    <td className="px-6 py-4 flex space-x-2">
                      <button onClick={() => handleEditJob(job.id)} className="text-blue-500 hover:text-blue-700">
                        <Edit className="h-5 w-5" />
                      </button>
                      <button onClick={() => setDeleteModal({ show: true, jobId: job.id })} className="text-red-500 hover:text-red-700">
                        <Trash2 className="h-5 w-5" />
                      </button>
                    </td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </motion.div>
        )}

        {/* Delete Confirmation Modal */}
        <AnimatePresence>
          {deleteModal.show && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
            >
              <motion.div
                initial={{ scale: 0.8 }}
                animate={{ scale: 1 }}
                exit={{ scale: 0.8 }}
                className="bg-white p-6 rounded-xl shadow-lg max-w-sm w-full"
              >
                <h3 className="text-lg font-bold text-gray-900 mb-4">Confirm Deletion</h3>
                <p className="text-gray-600 mb-6">Are you sure you want to delete this job?</p>
                <div className="flex justify-end space-x-3">
                  <Button variant="outline" onClick={() => setDeleteModal({ show: false, jobId: null })}>Cancel</Button>
                  <Button className="bg-red-600 hover:bg-red-700 text-white" onClick={() => deleteModal.jobId && handleDeleteJob(deleteModal.jobId)}>Delete</Button>
                </div>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

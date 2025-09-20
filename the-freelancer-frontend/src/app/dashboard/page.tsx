'use client';
import React from 'react';
import { useAppSelector } from '@/hooks/redux';
import { Briefcase, UserCircle, Plus, FileText, BarChart3, Settings, Star, Edit3, Eye } from 'lucide-react';
import { motion } from 'framer-motion';
import { useRouter } from 'next/navigation';
import InviteFreelancerModal from '@/components/InviteFreelancerModal';
import { getBackendUrl } from '@/config/api';

const DashboardPage = () => {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => ({
    user: state.auth.user,
    isAuthenticated: state.auth.isAuthenticated,
  }));
  const [gigs, setGigs] = React.useState([]);
  const [loadingGigs, setLoadingGigs] = React.useState(false);
  const [loadingJobs, setLoadingJobs] = React.useState(false);
  const [jobs, setJobs] = React.useState([]);
  const [jobsError, setJobsError] = React.useState('');
  const [gigsError, setGigsError] = React.useState('');
  const [inviteModal, setInviteModal] = React.useState<{
    isOpen: boolean;
    selectedJobId: number | null;
  }>({ isOpen: false, selectedJobId: null });

  React.useEffect(() => {
    if (!user) return;
    const fetchGigs = async () => {
      setLoadingGigs(true);
      setGigsError('');
      try {
        const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
        const axios = (await import('axios')).default;
        const res = await axios.get(`${getBackendUrl()}/api/gigs/my-gigs`, {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        console.log("api is "+ `${getBackendUrl()}/api/gigs/my-gigs`);
        console.log('user is', user);
        setGigs(res.data);
      } catch (err: any) {
        setGigsError(err.response?.data?.message || 'Failed to fetch gigs.');
      } finally {
        setLoadingGigs(false);
      }
    };

    const fetchJobs = async () => {
      // Placeholder for fetching jobs if needed in future
      // console.log("Fetching jobs for client");
      setLoadingJobs(true);
      setJobsError('');
      try {
        const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
        const axios = (await import('axios')).default;
        const res = await axios.get(`${getBackendUrl()}/api/jobs/my-jobs`, {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        setJobs(res.data); // Replace with actual fetch logic
        // console.log(jobs);
      } catch(err : any) {
        setJobsError(err.response?.data?.message || 'Failed to fetch jobs.');
      } finally {
        setLoadingJobs(false);
      }

    };

    if (user.role === 'FREELANCER') {
      fetchGigs();
    } else {
      fetchJobs();
    }
  }, [user]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4 py-8">
      <div className="max-w-7xl mx-auto">
        
        {/* Hero Section */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          {/* <div className="flex items-center justify-center mb-6">
            <img
              src="https://images.unsplash.com/photo-1755278338891-e8d8481ff087?q=80&w=3072&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
              alt="Profile"
              className="object-cover h-20 w-20 rounded-full border-4 border-white shadow-lg"
            />
          </div> */}
          <motion.h1
            initial={{ scale: 0.9, opacity: 0.8 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.8, type: 'spring' }}
            className="text-4xl font-extrabold bg-gradient-to-r from-blue-600 via-purple-600 to-cyan-600 bg-clip-text text-transparent mb-2"
          >
            Welcome back, {user?.name || 'User'}!
          </motion.h1>
          <p className="text-gray-600 text-lg">
            {user?.role === 'FREELANCER' ? 'Ready to showcase your skills?' : 'Ready to find the perfect freelancer?'}
          </p>
        </motion.div>

        {/* Role-based Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
          {user?.role === 'FREELANCER' ? (
            // FREELANCER Actions
            <>
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                whileHover={{ scale: 1.05, rotateY: 5 }}
                className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-2xl p-6 shadow-xl cursor-pointer"
                onClick={() => window.location.href = '/dashboard/gigs/create'}
              >
                <div className="flex items-center mb-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <Plus className="h-8 w-8" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">Create New Gig</h3>
                <p className="text-blue-100">Showcase your skills and start earning</p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                whileHover={{ scale: 1.05, rotateY: 5 }}
                className="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-2xl p-6 shadow-xl cursor-pointer"
              >
                <div className="flex items-center mb-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <BarChart3 className="h-8 w-8" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">Analytics</h3>
                <p className="text-green-100">Track your gig performance</p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                whileHover={{ scale: 1.05, rotateY: 5 }}
                className="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-2xl p-6 shadow-xl cursor-pointer"
              >
                <div className="flex items-center mb-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <Star className="h-8 w-8" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">Reviews</h3>
                <p className="text-purple-100">Manage client feedback</p>
              </motion.div>
            </>
          ) : (
            // Client Actions
            <>
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                whileHover={{ scale: 1.05, rotateY: 5 }}
                className="bg-gradient-to-br from-orange-500 to-orange-600 text-white rounded-2xl p-6 shadow-xl cursor-pointer"
                onClick={() => window.location.href = '/jobs/post'}
              >
                <div className="flex items-center mb-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <FileText className="h-8 w-8" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">Post a Job</h3>
                <p className="text-orange-100">Find the perfect FREELANCER</p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                whileHover={{ scale: 1.05, rotateY: 5 }}
                className="bg-gradient-to-br from-cyan-500 to-cyan-600 text-white rounded-2xl p-6 shadow-xl cursor-pointer"
                onClick={() => router.push('/dashboard/projects')}
              >
                <div className="flex items-center mb-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <Briefcase className="h-8 w-8" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">Manage Projects</h3>
                <p className="text-cyan-100">Track ongoing work</p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                whileHover={{ scale: 1.05, rotateY: 5 }}
                className="bg-gradient-to-br from-pink-500 to-pink-600 text-white rounded-2xl p-6 shadow-xl cursor-pointer"
              >
                <div className="flex items-center mb-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <Settings className="h-8 w-8" />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-2">Account Settings</h3>
                <p className="text-pink-100">Manage your profile</p>
              </motion.div>
            </>
          )}
        </div>

        {/* Content Section */}
        {user?.role === 'FREELANCER' ? (
          /* FREELANCER's Gigs Section */
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="bg-white/80 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-8"
          >
            <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              <Briefcase className="h-6 w-6 text-blue-500" />
              Your Gigs
            </h2>
            {loadingGigs && (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                <span className="ml-2 text-blue-500">Loading gigs...</span>
              </div>
            )}
            {gigsError && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
                <p className="text-red-600">{gigsError}</p>
              </div>
            )}
            {!loadingGigs && gigs.length === 0 && (
              <div className="text-center py-12">
                <Briefcase className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500 text-lg">No gigs created yet</p>
                <p className="text-gray-400">Start by creating your first gig!</p>
              </div>
            )}
            <div className="grid gap-4">
              {gigs.map((gig: any, index: number) => (
                <motion.div
                  key={gig.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.4, delay: index * 0.1 }}
                  className="bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl p-6 shadow-md hover:shadow-lg transition-all duration-300"
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="font-bold text-xl text-gray-800">{gig.title}</h3>
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                      gig.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
                    }`}>
                      {gig.active ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                  <p className="text-gray-600 mb-4 line-clamp-2">{gig.description}</p>
                  <div className="flex flex-wrap gap-3 text-sm">
                    <span className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full">
                      {gig.category}
                    </span>
                    <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full">
                      ${gig.startingPrice}
                    </span>
                    <span className="bg-purple-100 text-purple-700 px-3 py-1 rounded-full">
                      {gig.deliveryDays} days
                    </span>
                  </div>
                  {gig.tags && gig.tags.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-2">
                      {gig.tags.map((tag: string, tagIndex: number) => (
                        <span
                          key={tagIndex}
                          className="bg-gray-200 text-gray-600 px-2 py-1 rounded text-xs"
                        >
                          #{tag}
                        </span>
                      ))}
                    </div>
                  )}
                  
                  {/* Action Buttons */}
                  <div className="mt-4 pt-4 border-t border-gray-200 flex justify-end space-x-3">
                    <motion.button
                      whileHover={{ scale: 1.05, y: -2 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={() => window.open(`/gigs/${gig.id}`, '_blank')}
                      className="flex items-center space-x-2 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 shadow-md hover:shadow-lg"
                    >
                      <Eye className="h-4 w-4" />
                      <span>View</span>
                    </motion.button>
                    
                    <motion.button
                      whileHover={{ scale: 1.05, y: -2 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={() => router.push(`/dashboard/gigs/edit/${gig.id}`)}
                      className="flex items-center space-x-2 bg-gradient-to-r from-purple-500 to-pink-600 hover:from-purple-600 hover:to-pink-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 shadow-md hover:shadow-lg"
                    >
                      <Edit3 className="h-4 w-4" />
                      <span>Edit</span>
                    </motion.button>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.div>
        ) : (
          /* Client's Jobs Section */
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="bg-white/80 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-8"
          >
            <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              <FileText className="h-6 w-6 text-orange-500" />
              Your Posted Jobs
            </h2>
            {loadingJobs && (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500"></div>
                <span className="ml-2 text-orange-500">Loading jobs...</span>
              </div>
            )}
            {jobsError && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
                <p className="text-red-600">{jobsError}</p>
              </div>
            )}
            {!loadingJobs && jobs.length === 0 && (
              <div className="text-center py-12">
                <FileText className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500 text-lg">No jobs posted yet</p>
                <p className="text-gray-400">Start by posting your first job!</p>
                <motion.button
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={() => window.location.href = '/jobs/post'}
                  className="mt-4 bg-gradient-to-r from-orange-500 to-orange-600 text-white px-6 py-3 rounded-lg font-semibold shadow-lg hover:shadow-xl transition-all duration-300"
                >
                  Post Your First Job
                </motion.button>
              </div>
            )}
            <div className="grid gap-4">
              {jobs.map((job: any, index: number) => (
                <motion.div
                  key={job.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.4, delay: index * 0.1 }}
                  className="bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl p-6 shadow-md hover:shadow-lg transition-all duration-300 border border-gray-200"
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="font-bold text-xl text-gray-800">{job.title || job.projectName}</h3>
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                      job.status === 'OPEN' ? 'bg-green-100 text-green-700' :
                      job.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-700' :
                      job.status === 'COMPLETED' ? 'bg-gray-100 text-gray-600' :
                      job.status === 'CANCELLED' ? 'bg-red-100 text-red-600' :
                      'bg-yellow-100 text-yellow-700'
                    }`}>
                      {job.status?.replace('_', ' ') || 'Draft'}
                    </span>
                  </div>
                  <p className="text-gray-600 mb-4 line-clamp-2">{job.description}</p>
                  
                  {/* Job Stats Row */}
                  <div className="flex flex-wrap gap-3 text-sm mb-4">
                    {job.category && (
                      <span className="bg-purple-100 text-purple-700 px-3 py-1 rounded-full">
                        {job.category}
                      </span>
                    )}
                    <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full">
                      ${job.minBudgetCents || job.budget_min || 0} - ${job.maxBudgetCents || job.budget_max || 0}
                    </span>
                    <span className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full">
                      {job.budgetType === 'HOURLY' ? 'Hourly' : 'Fixed Price'}
                    </span>
                    {job.isUrgent && (
                      <span className="bg-orange-100 text-orange-700 px-3 py-1 rounded-full">
                        🚀 Urgent
                      </span>
                    )}
                  </div>

                  {/* Skills Tags */}
                  {job.skills && job.skills.length > 0 && (
                    <div className="mb-4 flex flex-wrap gap-2">
                      {job.skills.slice(0, 5).map((skill: string, skillIndex: number) => (
                        <span
                          key={skillIndex}
                          className="bg-blue-50 text-blue-600 px-2 py-1 rounded text-xs border border-blue-200"
                        >
                          {skill}
                        </span>
                      ))}
                      {job.skills.length > 5 && (
                        <span className="text-gray-500 text-xs px-2 py-1">
                          +{job.skills.length - 5} more
                        </span>
                      )}
                    </div>
                  )}

                  {/* Job Actions and Info */}
                  <div className="flex justify-between items-center">
                    <div className="flex gap-4 text-sm text-gray-500">
                      <span className="flex items-center gap-1">
                        <UserCircle className="h-4 w-4" />
                        {job.proposalCount || Math.floor(Math.random() * 15)} proposals
                      </span>
                      <span className="flex items-center gap-1">
                        <Star className="h-4 w-4" />
                        Posted {new Date(job.createdAt || job.created_at || Date.now()).toLocaleDateString()}
                      </span>
                    </div>
                    
                    <div className="flex gap-2 flex-wrap">
                      <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => window.location.href = `/jobs/${job.id}`}
                        className="bg-blue-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors"
                      >
                        View Details
                      </motion.button>
                      <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => window.location.href = `/dashboard/jobs/edit/${job.id}`}
                        className="bg-gray-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-600 transition-colors"
                      >
                        Edit
                      </motion.button>
                      {job.status === 'OPEN' && (
                        <motion.button
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          onClick={() => setInviteModal({ isOpen: true, selectedJobId: job.id })}
                          className="bg-gradient-to-r from-purple-500 to-pink-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:from-purple-600 hover:to-pink-700 transition-all duration-200 flex items-center gap-2"
                        >
                          <UserCircle className="h-4 w-4" />
                          Invite Freelancers
                        </motion.button>
                      )}
                    </div>
                  </div>

                  {/* Additional Requirements */}
                  {(job.ndaRequired || job.ipAssignment) && (
                    <div className="mt-3 pt-3 border-t border-gray-200">
                      <div className="flex gap-2 text-xs">
                        {job.ndaRequired && (
                          <span className="bg-red-50 text-red-600 px-2 py-1 rounded">
                            NDA Required
                          </span>
                        )}
                        {job.ipAssignment && (
                          <span className="bg-indigo-50 text-indigo-600 px-2 py-1 rounded">
                            IP Assignment
                          </span>
                        )}
                      </div>
                    </div>
                  )}
                </motion.div>
              ))}
            </div>

            {/* Quick Actions for Clients */}
            {jobs.length > 0 && (
              <div className="mt-8 pt-6 border-t border-gray-200">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => window.location.href = '/jobs/post'}
                    className="bg-gradient-to-r from-orange-500 to-orange-600 text-white p-4 rounded-lg font-semibold text-center hover:shadow-lg transition-all duration-300"
                  >
                    <Plus className="h-5 w-5 mx-auto mb-2" />
                    Post Another Job
                  </motion.button>
                  
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => window.location.href = '/dashboard/jobs'}
                    className="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-4 rounded-lg font-semibold text-center hover:shadow-lg transition-all duration-300"
                  >
                    <Briefcase className="h-5 w-5 mx-auto mb-2" />
                    Manage All Jobs
                  </motion.button>
                  
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => window.location.href = '/gigs'}
                    className="bg-gradient-to-r from-purple-500 to-purple-600 text-white p-4 rounded-lg font-semibold text-center hover:shadow-lg transition-all duration-300"
                  >
                    <UserCircle className="h-5 w-5 mx-auto mb-2" />
                    Browse FREELANCERs
                  </motion.button>
                </div>
              </div>
            )}
          </motion.div>
        )}

      </div>

      {/* Invite Freelancer Modal */}
      {inviteModal.isOpen && inviteModal.selectedJobId && (
        <InviteFreelancerModal
          isOpen={inviteModal.isOpen}
          onClose={() => setInviteModal({ isOpen: false, selectedJobId: null })}
          jobId={inviteModal.selectedJobId}
          jobTitle={(() => {
            const job: any = jobs.find((job: any) => job.id === inviteModal.selectedJobId);
            return job?.title || job?.projectName || 'Unknown Job';
          })()}
          onInviteSent={() => {
            console.log('Invitation sent successfully!');
            // Optionally show a success notification
          }}
        />
      )}
    </div>
  );
};

export default DashboardPage;


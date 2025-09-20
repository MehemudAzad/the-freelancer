'use client';
import React from 'react';
import { useAppSelector } from '@/hooks/redux';
import { Briefcase, ArrowLeft, Calendar, DollarSign, Clock, CheckCircle, Star, MessageSquare } from 'lucide-react';
import { motion } from 'framer-motion';
import { useRouter } from 'next/navigation';
import ReviewModal from '@/components/ReviewModal';
import reviewService from '@/services/review';
import toast, { Toaster } from 'react-hot-toast';
import { getBackendUrl } from '@/config/api';

const ProjectsPage = () => {
  const router = useRouter();
  const { user } = useAppSelector((state) => ({
    user: state.auth.user,
  }));
  const [projects, setProjects] = React.useState<any[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');
  const [activeTab, setActiveTab] = React.useState<'ongoing' | 'finished'>('ongoing');
  
  // Review modal state
  const [reviewModalOpen, setReviewModalOpen] = React.useState(false);
  const [selectedProject, setSelectedProject] = React.useState<any>(null);
  const [projectReviews, setProjectReviews] = React.useState<{[key: string]: boolean}>({});

  React.useEffect(() => {
    if (!user) return;
    
    const fetchProjects = async () => {
      setLoading(true);
      setError('');
      try {
        const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
        const axios = (await import('axios')).default;
        const res = await axios.get(`${getBackendUrl()}/api/contracts/my-contracts`, {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        
        // Fetch additional freelancer details for each contract
        const projectsWithFreelancerInfo = await Promise.all(
          res.data.map(async (project: any) => {
            try {
              const freelancerRes = await axios.get(`${getBackendUrl()}/api/auth/public/users/${project.freelancerId}`, {
                headers: { Authorization: `Bearer ${accessToken}` },
              });
              return {
                ...project,
                freelancerName: freelancerRes.data.name,
                freelancerEmail: freelancerRes.data.email,
                // Ensure we have jobId - use the project ID if jobId is not available
                jobId: project.jobId || project.id.toString(),
              };
            } catch (error) {
              console.error(`Failed to fetch freelancer info for project ${project.id}:`, error);
              return {
                ...project,
                freelancerName: 'Unknown Freelancer',
                // Ensure we have jobId even if freelancer fetch fails
                jobId: project.jobId || project.id.toString(),
              };
            }
          })
        );
        
        setProjects(projectsWithFreelancerInfo);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to fetch projects.');
      } finally {
        setLoading(false);
      }
    };

    fetchProjects();
  }, [user]);

  // Check which projects already have reviews
  React.useEffect(() => {
    const checkExistingReviews = async () => {
      const finishedProjectIds = finishedProjects.map((project: any) => project.id);
      const reviewStatuses: {[key: string]: boolean} = {};
      
      await Promise.all(
        finishedProjectIds.map(async (projectId: string) => {
          try {
            const existingReview = await reviewService.getContractReview(projectId);
            reviewStatuses[projectId] = existingReview !== null;
          } catch (error) {
            console.error(`Error checking review for project ${projectId}:`, error);
            reviewStatuses[projectId] = false;
          }
        })
      );
      
      setProjectReviews(reviewStatuses);
    };

    if (finishedProjects.length > 0) {
      checkExistingReviews();
    }
  }, [projects]);

  const handleOpenReviewModal = (project: any) => {
    console.log('Opening review modal for project:', project);
    
    // Ensure we have all required data before opening modal
    if (!project.freelancerId) {
      toast.error('Unable to load freelancer information. Please try again.');
      return;
    }
    
    setSelectedProject(project);
    setReviewModalOpen(true);
  };

  const handleReviewSubmitted = () => {
    // Update the review status for this project
    if (selectedProject) {
      setProjectReviews(prev => ({
        ...prev,
        [selectedProject.id]: true
      }));
      
      // Show success message
      toast.success(`Review submitted for ${selectedProject.freelancerName || 'freelancer'}!`);
      
      // Close modal
      setReviewModalOpen(false);
      setSelectedProject(null);
    }
  };

  const ongoingProjects = projects.filter((project: any) => project.status === 'ACTIVE');
  const finishedProjects = projects.filter((project: any) => project.status !== 'ACTIVE');

  const currentProjects = activeTab === 'ongoing' ? ongoingProjects : finishedProjects;

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4 py-8">
      <div className="max-w-7xl mx-auto">
        
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-8"
        >
          <div className="flex items-center gap-4 mb-6">
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
          
          <h1 className="text-4xl font-extrabold bg-gradient-to-r from-blue-600 via-purple-600 to-cyan-600 bg-clip-text text-transparent mb-2">
            Manage Projects
          </h1>
          <p className="text-gray-600 text-lg">
            Track and manage all your ongoing and completed projects
          </p>
        </motion.div>

        {/* Tabs */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="mb-8"
        >
          <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
            <button
              onClick={() => setActiveTab('ongoing')}
              className={`px-6 py-3 rounded-md font-medium transition-all duration-200 ${
                activeTab === 'ongoing'
                  ? 'bg-white text-blue-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              <div className="flex items-center gap-2">
                <Clock className="h-4 w-4" />
                Ongoing ({ongoingProjects.length})
              </div>
            </button>
            <button
              onClick={() => setActiveTab('finished')}
              className={`px-6 py-3 rounded-md font-medium transition-all duration-200 ${
                activeTab === 'finished'
                  ? 'bg-white text-green-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              <div className="flex items-center gap-2">
                <CheckCircle className="h-4 w-4" />
                Finished ({finishedProjects.length})
              </div>
            </button>
          </div>
        </motion.div>

        {/* Projects Content */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="bg-white/80 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-8"
        >
          {loading && (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              <span className="ml-2 text-blue-500">Loading projects...</span>
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
              <p className="text-red-600">{error}</p>
            </div>
          )}

          {!loading && currentProjects.length === 0 && (
            <div className="text-center py-12">
              <Briefcase className="h-16 w-16 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-500 text-lg">
                No {activeTab} projects found
              </p>
              <p className="text-gray-400">
                {activeTab === 'ongoing' 
                  ? 'Award a proposal to start your first project!' 
                  : 'Complete some projects to see them here!'
                }
              </p>
            </div>
          )}

          <div className="grid gap-6">
            {currentProjects.map((project: any, index: number) => (
              <motion.div
                key={project.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.4, delay: index * 0.1 }}
                className="bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl p-6 shadow-md hover:shadow-lg transition-all duration-300 border border-gray-200"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="font-bold text-xl text-gray-800 mb-2">
                      {project.jobTitle}
                    </h3>
                    <div className="flex items-center gap-4 text-sm text-gray-600 mb-2">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-4 w-4" />
                        Started: {new Date(project.startDate).toLocaleDateString()}
                      </span>
                      {project.endDate && (
                        <span className="flex items-center gap-1">
                          <Calendar className="h-4 w-4" />
                          {activeTab === 'ongoing' ? 'Due:' : 'Completed:'} {new Date(project.endDate).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                    {/* Show freelancer info for clients */}
                    {(user?.role === 'client' || user?.role?.toLowerCase() === 'client') && project.freelancerName && (
                      <div className="text-sm text-gray-600 flex items-center gap-1">
                        <Briefcase className="h-4 w-4" />
                        Freelancer: <span className="font-medium text-gray-800">{project.freelancerName}</span>
                      </div>
                    )}
                  </div>
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                    project.status === 'ACTIVE' 
                      ? 'bg-blue-100 text-blue-700' 
                      : 'bg-green-100 text-green-700'
                    }`}>
                    {project.status === 'ACTIVE' ? 'Active' : 'Completed'}
                  </span>
                </div>

                <div className="flex flex-wrap gap-3 text-sm mb-4">
                  <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full flex items-center gap-1">
                    <DollarSign className="h-3 w-3" />
                    ${project.totalAmountCents}
                  </span>
                  <span className="bg-purple-100 text-purple-700 px-3 py-1 rounded-full">
                    Project ID: {project.id}
                  </span>
                </div>

                {/* Progress Information */}
                <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-medium text-gray-700">Progress</span>
                    <span className="text-sm text-gray-600">
                      {project.completedMilestones}/{project.totalMilestones} milestones
                    </span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                      style={{ 
                        width: `${project.totalMilestones > 0 
                          ? (project.completedMilestones / project.totalMilestones) * 100 
                          : 0}%` 
                      }}
                    ></div>
                  </div>
                </div>

                <div className="flex justify-end space-x-3">
                  {activeTab === 'ongoing' ? (
                    <motion.button
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={() => router.push(`/workspace/${project.id}`)}
                      className="bg-blue-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors"
                    >
                      Go to Workspace
                    </motion.button>
                  ) : (
                    <>
                      <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => router.push(`/workspace/${project.id}`)}
                        className="bg-gray-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-600 transition-colors"
                      >
                        View Details
                      </motion.button>
                      
                      {/* Review Button - Only show if user is a client and hasn't reviewed yet */}
                      {console.log('Debug Review Button:', {
                        userRole: user?.role,
                        projectId: project.id,
                        hasReview: projectReviews[project.id],
                        freelancerId: project.freelancerId,
                        freelancerName: project.freelancerName
                      })}
                      {(user?.role === 'client' || user?.role?.toLowerCase() === 'client') && !projectReviews[project.id] && project.freelancerId && (
                        <motion.button
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          onClick={() => handleOpenReviewModal(project)}
                          className="bg-gradient-to-r from-yellow-500 to-orange-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:from-yellow-600 hover:to-orange-600 transition-all duration-200 flex items-center space-x-2"
                        >
                          <Star className="w-4 h-4" />
                          <span>Write Review</span>
                        </motion.button>
                      )}
                      
                      {/* Show message if freelancer info is missing */}
                      {(user?.role === 'client' || user?.role?.toLowerCase() === 'client') && !projectReviews[project.id] && !project.freelancerId && (
                        <div className="bg-gray-100 text-gray-600 px-4 py-2 rounded-lg text-sm font-medium flex items-center space-x-2">
                          <MessageSquare className="w-4 h-4" />
                          <span>Loading freelancer info...</span>
                        </div>
                      )}
                      
                      {/* Review Submitted Indicator */}
                      {(user?.role === 'client' || user?.role?.toLowerCase() === 'client') && projectReviews[project.id] && (
                        <motion.div
                          initial={{ scale: 0.8, opacity: 0 }}
                          animate={{ scale: 1, opacity: 1 }}
                          className="bg-gradient-to-r from-green-100 to-emerald-100 text-green-800 px-4 py-2 rounded-lg text-sm font-medium flex items-center space-x-2 border border-green-200"
                        >
                          <CheckCircle className="w-4 h-4" />
                          <span>Review Submitted</span>
                        </motion.div>
                      )}
                      
                      {/* Debug: Always show review button for clients on finished projects */}
                      {/* {activeTab === 'finished' && (
                        <motion.button
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          onClick={() => {
                            console.log('Debug button clicked for project:', project);
                            console.log('User role:', user?.role);
                            console.log('User object:', user);
                            handleOpenReviewModal(project);
                          }}
                          className="bg-gradient-to-r from-purple-500 to-pink-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:from-purple-600 hover:to-pink-600 transition-all duration-200 flex items-center space-x-2"
                        >
                          <Star className="w-4 h-4" />
                          <span>Debug Review (Role: {user?.role})</span>
                        </motion.button>
                      )} */}
                    </>
                  )}
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Review Modal */}
        {selectedProject && (
          <ReviewModal
            isOpen={reviewModalOpen}
            onClose={() => {
              setReviewModalOpen(false);
              setSelectedProject(null);
            }}
            project={{
              id: selectedProject.id,
              jobTitle: selectedProject.jobTitle,
              freelancerId: selectedProject.freelancerId,
              jobId: selectedProject.jobId || selectedProject.id.toString(),
              freelancerName: selectedProject.freelancerName,
            }}
            onReviewSubmitted={handleReviewSubmitted}
          />
        )}

        {/* Toast Notifications */}
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#363636',
              color: '#fff',
            },
            success: {
              duration: 3000,
              iconTheme: {
                primary: '#4ade80',
                secondary: '#fff',
              },
            },
            error: {
              duration: 5000,
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
          }}
        />

      </div>
    </div>
  );
};

export default ProjectsPage;
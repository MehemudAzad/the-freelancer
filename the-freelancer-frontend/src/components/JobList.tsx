// import React, { useState, useEffect } from 'react';
// import { useRouter } from 'next/navigation';
// import { motion, AnimatePresence } from 'framer-motion';
// import { 
//   Search, 
//   MapPin, 
//   DollarSign, 
//   Clock, 
//   Star, 
//   User,
//   Calendar,
//   Briefcase,
//   MessageSquare,
//   Bookmark,
//   Share2,
//   Heart,
//   MoreHorizontal,
//   CheckCircle,
//   Eye,
//   ThumbsUp,
//   Sparkles,
//   Zap,
//   Award,
//   TrendingUp,
//   Users
// } from 'lucide-react';
// import { Job } from '@/types/api';
// import { Button } from '@/components/ui/button';
// import { formatDistanceToNow } from 'date-fns';
// import { useAppSelector } from '@/store/hooks';

// interface JobCardProps {
//   job: Job;
//   onClick?: () => void;
// }

// interface JobCardProps {
//   job: Job;
//   onClick?: () => void;
// }

// const JobCard = ({ job, onClick }: JobCardProps) => {
//   const router = useRouter();
//   const [isLiked, setIsLiked] = useState(false);
//   const [isSaved, setIsSaved] = useState(false);
//   const [showFullDescription, setShowFullDescription] = useState(false);
//   const [isClient, setIsClient] = useState(false);

//   // console.log('in the job',job);
  
//   // Get user auth state
//   const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  
//   // Ensure we're on the client side to prevent hydration mismatch
//   useEffect(() => {
//     setIsClient(true);
//   }, []);
  
//   // Check if user is a freelancer (only on client side)
//   const isFreelancer = isClient && isAuthenticated && 
//     (user?.role?.toLowerCase() === 'freelancer' || user?.role?.toUpperCase() === 'FREELANCER');
  
//   // Don't render interactive elements until client-side hydration is complete
//   if (!isClient) {
//     return (
//       <div className="bg-white rounded-2xl shadow-lg border border-gray-100 mb-6 overflow-hidden">
//         <div className="p-6 animate-pulse">
//           <div className="flex items-center space-x-3 mb-4">
//             <div className="w-12 h-12 bg-gray-200 rounded-full"></div>
//             <div className="flex-1">
//               <div className="h-4 bg-gray-200 rounded w-32 mb-2"></div>
//               <div className="h-3 bg-gray-200 rounded w-24"></div>
//             </div>
//           </div>
//           <div className="h-6 bg-gray-200 rounded mb-3"></div>
//           <div className="h-20 bg-gray-200 rounded mb-4"></div>
//           <div className="flex justify-between items-center">
//             <div className="flex space-x-4">
//               <div className="h-8 bg-gray-200 rounded w-16"></div>
//               <div className="h-8 bg-gray-200 rounded w-20"></div>
//             </div>
//             <div className="h-8 bg-gray-200 rounded w-24"></div>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   const formatBudget = (min?: number, max?: number) => {
//     if (min && max) {
//       return `$${min.toLocaleString()} - $${max.toLocaleString()}`;
//     } else if (min) {
//       return `From $${min.toLocaleString()}`;
//     } else if (max) {
//       return `Up to $${max.toLocaleString()}`;
//     }
//     return 'Budget not specified';
//   };

//   const getStatusBadge = (status: string) => {
//     const styles: Record<string, string> = {
//       open: 'bg-green-100 text-green-800 border-green-200',
//       in_progress: 'bg-blue-100 text-blue-800 border-blue-200',
//       completed: 'bg-gray-100 text-gray-800 border-gray-200',
//       cancelled: 'bg-red-100 text-red-800 border-red-200',
//     };
//     return (
//       <span className={`px-3 py-1 rounded-full text-sm font-semibold border ${styles[status?.toLowerCase()] || 'bg-gray-100 text-gray-800 border-gray-200'}`}>
//         {status?.replace('_', ' ').toUpperCase() || 'UNKNOWN'}
//       </span>
//     );
//   };

//   const formatDate = (dateString: string | Date | undefined) => {
//     if (!dateString) return 'N/A';
//     return formatDistanceToNow(new Date(dateString), { addSuffix: true });
//   };

//   const description = job.description || '';
//   const shouldTruncate = description.length > 300;
//   const displayDescription = showFullDescription ? description : description.slice(0, 300) + (shouldTruncate ? '...' : '');

//   return (
//     <div className="bg-white rounded-2xl shadow-lg border border-gray-100 mb-6 overflow-hidden hover:shadow-xl transition-all duration-300">
//       {/* Header - Client Info */}
//       <div className="p-6 border-b border-gray-100">
//         <div className="flex items-center justify-between">
//           <div className="flex items-center space-x-3">
//             <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-lg">
//               {job.client?.name ? job.client.name.charAt(0).toUpperCase() : 'C'}
//             </div>
//             <div>
//               <h4 className="font-semibold text-gray-900">
//                 {job.client?.name || 'Client'}
//               </h4>
//               <div className="flex items-center text-sm text-gray-500">
//                 <span>Posted {formatDate(job.createdAt || job.created_at)}</span>
//                 <span className="mx-2">•</span>
//                 <span className="flex items-center">
//                   <Eye className="h-3 w-3 mr-1" />
//                   {Math.floor(Math.random() * 100) + 10} views
//                 </span>
//               </div>
//             </div>
//           </div>
//           <div className="flex items-center space-x-2">
//             {getStatusBadge(job.status)}
//             <Button variant="ghost" size="sm">
//               <MoreHorizontal className="h-4 w-4" />
//             </Button>
//           </div>
//         </div>
//       </div>

//       {/* Job Content */}
//       <div className="p-6">
//         {/* Title */}
//         <h2 className="text-xl font-bold text-gray-900 mb-3 leading-tight">
//           {job.title || job.projectName}
//         </h2>

//         {/* Budget and Type - Prominent Display */}
//         <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-xl p-4 mb-4 border border-green-100">
//           <div className="flex items-center justify-between">
//             <div className="flex items-center space-x-4">
//               <div className="flex items-center text-green-600">
//                 <DollarSign className="h-5 w-5 mr-1" />
//                 <span className="text-lg font-bold">
//                   {formatBudget(job.minBudgetCents ? job.minBudgetCents / 100 : undefined, job.maxBudgetCents ? job.maxBudgetCents / 100 : undefined)}
//                 </span>
//               </div>
//               <div className="text-sm text-gray-600">
//                 {job.budgetType === 'HOURLY' ? 'Per Hour' : 'Fixed Price'}
//               </div>
//             </div>
//             {job.isUrgent && (
//               <div className="bg-orange-100 text-orange-800 px-3 py-1 rounded-full text-sm font-medium">
//                 🚀 URGENT
//               </div>
//             )}
//           </div>
//         </div>

//         {/* Description */}
//         <div className="mb-4">
//           <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">
//             {displayDescription}
//           </p>
//           {shouldTruncate && (
//             <button
//               onClick={() => setShowFullDescription(!showFullDescription)}
//               className="text-blue-600 hover:text-blue-700 text-sm font-medium mt-2"
//             >
//               {showFullDescription ? 'Show less' : 'Read more'}
//             </button>
//           )}
//         </div>

//         {/* Skills */}
//         {job.skills && job.skills.length > 0 && (
//           <div className="mb-4">
//             <h4 className="text-sm font-semibold text-gray-700 mb-2">Required Skills:</h4>
//             <div className="flex flex-wrap gap-2">
//               {job.skills.map((skill: string, index: number) => (
//                 <span 
//                   key={index}
//                   className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium hover:bg-blue-200 transition-colors"
//                 >
//                   {skill}
//                 </span>
//               ))}
//             </div>
//           </div>
//         )}

//         {/* Job Details Grid */}
//         <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4 text-sm">
//           {job.category && (
//             <div className="flex items-center text-gray-600">
//               <Briefcase className="h-4 w-4 mr-2" />
//               <span>{job.category}</span>
//             </div>
//           )}
//           {job.deadline && (
//             <div className="flex items-center text-gray-600">
//               <Calendar className="h-4 w-4 mr-2" />
//               <span>Due: {new Date(job.deadline).toLocaleDateString()}</span>
//             </div>
//           )}
//           <div className="flex items-center text-gray-600">
//             <MessageSquare className="h-4 w-4 mr-2" />
//             <span>{Math.floor(Math.random() * 15)} proposals</span>
//           </div>
//           <div className="flex items-center text-gray-600">
//             <Star className="h-4 w-4 mr-2 text-yellow-400 fill-current" />
//             <span>4.8 (12 reviews)</span>
//           </div>
//         </div>

//         {/* Additional Requirements */}
//         {(job.ndaRequired || job.ipAssignment) && (
//           <div className="mb-4">
//             <h4 className="text-sm font-semibold text-gray-700 mb-2">Requirements:</h4>
//             <div className="flex flex-wrap gap-2">
//               {job.ndaRequired && (
//                 <div className="flex items-center bg-purple-50 text-purple-700 px-3 py-1 rounded-full text-xs">
//                   <CheckCircle className="h-3 w-3 mr-1" />
//                   NDA Required
//                 </div>
//               )}
//               {job.ipAssignment && (
//                 <div className="flex items-center bg-indigo-50 text-indigo-700 px-3 py-1 rounded-full text-xs">
//                   <CheckCircle className="h-3 w-3 mr-1" />
//                   IP Rights Assignment
//                 </div>
//               )}
//             </div>
//           </div>
//         )}
//       </div>

//       {/* Actions Footer */}
//       <div className="border-t border-gray-100 px-6 py-4">
//         <div className="flex items-center justify-between">
//           <div className="flex items-center space-x-4">
//             <Button
//               variant="ghost"
//               size="sm"
//               onClick={() => setIsLiked(!isLiked)}
//               className={`flex items-center space-x-2 ${isLiked ? 'text-red-500' : 'text-gray-500'}`}
//             >
//               <Heart className={`h-4 w-4 ${isLiked ? 'fill-current' : ''}`} />
//               <span className="text-sm">{isLiked ? 'Liked' : 'Like'}</span>
//             </Button>
            
//             <Button variant="ghost" size="sm" className="flex items-center space-x-2 text-gray-500">
//               <MessageSquare className="h-4 w-4" />
//               <span className="text-sm">Comment</span>
//             </Button>
            
//             <Button variant="ghost" size="sm" className="flex items-center space-x-2 text-gray-500">
//               <Share2 className="h-4 w-4" />
//               <span className="text-sm">Share</span>
//             </Button>
//           </div>
          
//           <div className="flex items-center space-x-2">
//             <Button
//               variant="ghost"
//               size="sm"
//               onClick={() => setIsSaved(!isSaved)}
//               className={`${isSaved ? 'text-blue-500' : 'text-gray-500'}`}
//             >
//               <Bookmark className={`h-4 w-4 ${isSaved ? 'fill-current' : ''}`} />
//             </Button>
            
//             {/* Only show Apply button for authenticated freelancers */}
//             {isFreelancer ? (
//               <Button 
//                 onClick={() => router.push(`/jobs/proposal?jobId=${job.id}`)}
//                 className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2"
//               >
//                 Apply Now
//               </Button>
//             ) : isAuthenticated && (user?.role?.toLowerCase() === 'client' || user?.role?.toUpperCase() === 'CLIENT') ? (
//               <Button 
//                 disabled
//                 className="bg-gray-400 cursor-not-allowed text-white px-6 py-2"
//                 title="Only freelancers can apply to jobs"
//               >
//                 Clients Cannot Apply
//               </Button>
//             ) : !isAuthenticated ? (
//               <Button 
//                 onClick={() => {
//                   // Redirect to login if not authenticated
//                   window.location.href = '/auth/login';
//                 }}
//                 className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2"
//               >
//                 Login to Apply
//               </Button>
//             ) : null}
//           </div>
//         </div>
//       </div>
//     </div>
//   );
// };

// interface JobListProps {
//   jobs: Job[];
//   onJobClick?: (job: Job) => void;
//   loading?: boolean;
// }

// const JobList = ({ jobs, onJobClick, loading }: JobListProps) => {
//   if (loading) {
//     return (
//       <div className="space-y-6">
//         {[...Array(4)].map((_, index) => (
//           <div key={index} className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6 animate-pulse">
//             <div className="flex items-center space-x-3 mb-4">
//               <div className="w-12 h-12 bg-gray-200 rounded-full"></div>
//               <div className="flex-1">
//                 <div className="h-4 bg-gray-200 rounded w-32 mb-2"></div>
//                 <div className="h-3 bg-gray-200 rounded w-24"></div>
//               </div>
//               <div className="h-6 bg-gray-200 rounded-full w-16"></div>
//             </div>
//             <div className="h-6 bg-gray-200 rounded mb-3"></div>
//             <div className="h-20 bg-gray-200 rounded mb-4"></div>
//             <div className="flex gap-2 mb-4">
//               <div className="h-6 bg-gray-200 rounded-full w-20"></div>
//               <div className="h-6 bg-gray-200 rounded-full w-24"></div>
//               <div className="h-6 bg-gray-200 rounded-full w-16"></div>
//             </div>
//             <div className="h-16 bg-gray-200 rounded mb-4"></div>
//             <div className="flex justify-between items-center">
//               <div className="flex space-x-4">
//                 <div className="h-8 bg-gray-200 rounded w-16"></div>
//                 <div className="h-8 bg-gray-200 rounded w-20"></div>
//                 <div className="h-8 bg-gray-200 rounded w-16"></div>
//               </div>
//               <div className="h-8 bg-gray-200 rounded w-24"></div>
//             </div>
//           </div>
//         ))}
//       </div>
//     );
//   }

//   if (jobs.length === 0) {
//     return (
//       <div className="text-center py-12">
//         <div className="text-gray-400 mb-4">
//           <Search className="h-16 w-16 mx-auto" />
//         </div>
//         <h3 className="text-lg font-medium text-gray-900 mb-2">No jobs found</h3>
//         <p className="text-gray-500">Try adjusting your search criteria or filters.</p>
//       </div>
//     );
//   }

//   return (
//     <div className="space-y-6">
//       {jobs.map((job) => (
//         <JobCard 
//           key={job.id} 
//           job={job} 
//           onClick={() => onJobClick?.(job)}
//         />
//       ))}
//     </div>
//   );
// };

// export default JobList;


import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Search, 
  MapPin, 
  DollarSign, 
  Clock, 
  Star, 
  User,
  Calendar,
  Briefcase,
  MessageSquare,
  Bookmark,
  Share2,
  Heart,
  MoreHorizontal,
  CheckCircle,
  Eye,
  ThumbsUp,
  Sparkles,
  Zap,
  Award,
  TrendingUp,
  Users,
  Paperclip,
  ExternalLink
} from 'lucide-react';
import { Job, Attachment } from '@/types/api';
import { Button } from '@/components/ui/button';
import { formatDistanceToNow } from 'date-fns';
import { useAppSelector } from '@/store/hooks';
import jobService from '@/services/job';

interface JobCardProps {
  job: Job;
  onClick?: () => void;
}

interface JobCardProps {
  job: Job;
  onClick?: () => void;
}

const JobCard = ({ job, onClick }: JobCardProps) => {
  const router = useRouter();
  const [isLiked, setIsLiked] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [showFullDescription, setShowFullDescription] = useState(false);
  const [isClient, setIsClient] = useState(false);
  const [attachments, setAttachments] = useState<Attachment[]>([]);
  const [loadingAttachments, setLoadingAttachments] = useState(false);
  
  // Get user auth state
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  
  // Ensure we're on the client side to prevent hydration mismatch
  useEffect(() => {
    setIsClient(true);
  }, []);

  // Fetch attachments for this job
  useEffect(() => {
    const fetchAttachments = async () => {
      if (!job.id) return;
      
      try {
        setLoadingAttachments(true);
        const jobAttachments = await jobService.jobAPI.getJobAttachments(job.id);
        setAttachments(jobAttachments);
      } catch (error) {
        console.error('Error fetching job attachments:', error);
        // Fail silently - attachments are not critical for job listing
      } finally {
        setLoadingAttachments(false);
      }
    };

    if (isClient && job.id) {
      fetchAttachments();
    }
  }, [job.id, isClient]);
  
  // Check if user is a freelancer (only on client side)
  const isFreelancer = isClient && isAuthenticated && 
    (user?.role?.toLowerCase() === 'freelancer' || user?.role?.toUpperCase() === 'FREELANCER');
  
  // Don't render interactive elements until client-side hydration is complete
  if (!isClient) {
    return (
      <div className="bg-white rounded-2xl shadow-lg border border-gray-100 mb-6 overflow-hidden">
        <div className="p-6 animate-pulse">
          <div className="flex items-center space-x-3 mb-4">
            <div className="w-12 h-12 bg-gray-200 rounded-full"></div>
            <div className="flex-1">
              <div className="h-4 bg-gray-200 rounded w-32 mb-2"></div>
              <div className="h-3 bg-gray-200 rounded w-24"></div>
            </div>
          </div>
          <div className="h-6 bg-gray-200 rounded mb-3"></div>
          <div className="h-20 bg-gray-200 rounded mb-4"></div>
          <div className="flex justify-between items-center">
            <div className="flex space-x-4">
              <div className="h-8 bg-gray-200 rounded w-16"></div>
              <div className="h-8 bg-gray-200 rounded w-20"></div>
            </div>
            <div className="h-8 bg-gray-200 rounded w-24"></div>
          </div>
        </div>
      </div>
    );
  }

  const formatBudget = (min?: number, max?: number) => {
    if (min && max) {
      return `$${min.toLocaleString()} - $${max.toLocaleString()}`;
    } else if (min) {
      return `From $${min.toLocaleString()}`;
    } else if (max) {
      return `Up to $${max.toLocaleString()}`;
    }
    return 'Budget not specified';
  };

  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      open: 'bg-green-100 text-green-800 border-green-200',
      in_progress: 'bg-blue-100 text-blue-800 border-blue-200',
      completed: 'bg-gray-100 text-gray-800 border-gray-200',
      cancelled: 'bg-red-100 text-red-800 border-red-200',
    };
    return (
      <span className={`px-3 py-1 rounded-full text-sm font-semibold border ${styles[status?.toLowerCase()] || 'bg-gray-100 text-gray-800 border-gray-200'}`}>
        {status?.replace('_', ' ').toUpperCase() || 'UNKNOWN'}
      </span>
    );
  };

  const formatDate = (dateString: string | Date | undefined) => {
    if (!dateString) return 'N/A';
    return formatDistanceToNow(new Date(dateString), { addSuffix: true });
  };

  const description = job.description || '';
  const shouldTruncate = description.length > 300;
  const displayDescription = showFullDescription ? description : description.slice(0, 300) + (shouldTruncate ? '...' : '');

  return (
    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 mb-6 overflow-hidden hover:shadow-xl transition-all duration-300">
      {/* Header - Client Info */}
      <div className="p-6 border-b border-gray-100">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-lg">
              {job.client?.name ? job.client.name.charAt(0).toUpperCase() : 'C'}
            </div>
            <div>
              <h4 className="font-semibold text-gray-900">
                {job.client?.name || 'Client'}
              </h4>
              <div className="flex items-center text-sm text-gray-500">
                <span>Posted {formatDate(job.createdAt || job.created_at)}</span>
                <span className="mx-2">•</span>
                {/* <span className="flex items-center">
                  <Eye className="h-3 w-3 mr-1" />
                  {Math.floor(Math.random() * 100) + 10} views
                </span> */}
              </div>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            {getStatusBadge(job.status)}
            <Button variant="ghost" size="sm">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Job Content */}
      <div className="p-6">
        {/* Title */}
        <h2 className="text-xl font-bold text-gray-900 mb-3 leading-tight">
          {job.title || job.projectName}
        </h2>

        {/* Budget and Type - Prominent Display */}
        <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-xl p-4 mb-4 border border-green-100">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="flex items-center text-green-600">
                <DollarSign className="h-5 w-5 mr-1" />
                <span className="text-lg font-bold">
                  {formatBudget(job.minBudgetCents ? job.minBudgetCents / 100 : undefined, job.maxBudgetCents ? job.maxBudgetCents / 100 : undefined)}
                </span>
              </div>
              <div className="text-sm text-gray-600">
                {job.budgetType === 'HOURLY' ? 'Per Hour' : 'Fixed Price'}
              </div>
            </div>
            {job.isUrgent && (
              <div className="bg-orange-100 text-orange-800 px-3 py-1 rounded-full text-sm font-medium">
                🚀 URGENT
              </div>
            )}
          </div>
        </div>

        {/* Description */}
        <div className="mb-4">
          <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">
            {displayDescription}
          </p>
          {shouldTruncate && (
            <button
              onClick={() => setShowFullDescription(!showFullDescription)}
              className="text-blue-600 hover:text-blue-700 text-sm font-medium mt-2"
            >
              {showFullDescription ? 'Show less' : 'Read more'}
            </button>
          )}
        </div>

        {/* Skills */}
        {job.skills && job.skills.length > 0 && (
          <div className="mb-4">
            <h4 className="text-sm font-semibold text-gray-700 mb-2">Required Skills:</h4>
            <div className="flex flex-wrap gap-2">
              {job.skills.map((skill: string, index: number) => (
                <span 
                  key={index}
                  className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium hover:bg-blue-200 transition-colors"
                >
                  {skill}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Job Details Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4 text-sm">
          {job.category && (
            <div className="flex items-center text-gray-600">
              <Briefcase className="h-4 w-4 mr-2" />
              <span>{job.category}</span>
            </div>
          )}
          {job.deadline && (
            <div className="flex items-center text-gray-600">
              <Calendar className="h-4 w-4 mr-2" />
              <span>Due: {new Date(job.deadline).toLocaleDateString()}</span>
            </div>
          )}
          {/* <div className="flex items-center text-gray-600">
            <MessageSquare className="h-4 w-4 mr-2" />
            <span>{Math.floor(Math.random() * 15)} proposals</span>
          </div> */}
          {/* <div className="flex items-center text-gray-600">
            <Star className="h-4 w-4 mr-2 text-yellow-400 fill-current" />
            <span>4.8 (12 reviews)</span>
          </div> */}
        </div>

        {/* Additional Requirements */}
        {(job.ndaRequired || job.ipAssignment) && (
          <div className="mb-4">
            <h4 className="text-sm font-semibold text-gray-700 mb-2">Requirements:</h4>
            <div className="flex flex-wrap gap-2">
              {job.ndaRequired && (
                <div className="flex items-center bg-purple-50 text-purple-700 px-3 py-1 rounded-full text-xs">
                  <CheckCircle className="h-3 w-3 mr-1" />
                  NDA Required
                </div>
              )}
              {job.ipAssignment && (
                <div className="flex items-center bg-indigo-50 text-indigo-700 px-3 py-1 rounded-full text-xs">
                  <CheckCircle className="h-3 w-3 mr-1" />
                  IP Rights Assignment
                </div>
              )}
            </div>
          </div>
        )}

        {/* Attachments */}
        {attachments.length > 0 && (
          <div className="mb-4">
            <h4 className="text-sm font-semibold text-gray-700 mb-2 flex items-center">
              <Paperclip className="h-4 w-4 mr-1" />
              Attachments ({attachments.length})
            </h4>
            <div className="bg-gray-50 rounded-lg p-3 space-y-2">
              {attachments.map((attachment) => (
                <div key={attachment.id} className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <div className="text-lg">
                      {attachment.contentType === 'application/pdf' ? '📄' : 
                       attachment.contentType?.startsWith('image/') ? '🖼️' : '📁'}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{attachment.filename || 'Attachment'}</p>
                      <p className="text-xs text-gray-500">
                        {(attachment.bytes / 1024).toFixed(1)} KB
                      </p>
                    </div>
                  </div>
                  <a
                    href={attachment.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center space-x-1 text-blue-600 hover:text-blue-700 text-sm font-medium"
                  >
                    <ExternalLink className="w-3 h-3" />
                    <span>View</span>
                  </a>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Actions Footer */}
      <div className="border-t border-gray-100 px-6 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            
            {/* Only show Apply button for authenticated freelancers */}
            {isFreelancer ? (
              <Button 
                onClick={() => router.push(`/jobs/proposal?jobId=${job.id}`)}
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2"
              >
                Apply Now
              </Button>
            ) : isAuthenticated && (user?.role?.toLowerCase() === 'client' || user?.role?.toUpperCase() === 'CLIENT') ? (
              <Button 
                disabled
                className="bg-gray-400 cursor-not-allowed text-white px-6 py-2"
                title="Only freelancers can apply to jobs"
              >
                Clients Cannot Apply
              </Button>
            ) : !isAuthenticated ? (
              <Button 
                onClick={() => {
                  // Redirect to login if not authenticated
                  window.location.href = '/auth/login';
                }}
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2"
              >
                Login to Apply
              </Button>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
};

interface JobListProps {
  jobs: Job[];
  onJobClick?: (job: Job) => void;
  loading?: boolean;
}

const JobList = ({ jobs, onJobClick, loading }: JobListProps) => {
  if (loading) {
    return (
      <div className="space-y-6">
        {[...Array(4)].map((_, index) => (
          <div key={index} className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6 animate-pulse">
            <div className="flex items-center space-x-3 mb-4">
              <div className="w-12 h-12 bg-gray-200 rounded-full"></div>
              <div className="flex-1">
                <div className="h-4 bg-gray-200 rounded w-32 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded w-24"></div>
              </div>
              <div className="h-6 bg-gray-200 rounded-full w-16"></div>
            </div>
            <div className="h-6 bg-gray-200 rounded mb-3"></div>
            <div className="h-20 bg-gray-200 rounded mb-4"></div>
            <div className="flex gap-2 mb-4">
              <div className="h-6 bg-gray-200 rounded-full w-20"></div>
              <div className="h-6 bg-gray-200 rounded-full w-24"></div>
              <div className="h-6 bg-gray-200 rounded-full w-16"></div>
            </div>
            <div className="h-16 bg-gray-200 rounded mb-4"></div>
            <div className="flex justify-between items-center">
              <div className="flex space-x-4">
                <div className="h-8 bg-gray-200 rounded w-16"></div>
                <div className="h-8 bg-gray-200 rounded w-20"></div>
                <div className="h-8 bg-gray-200 rounded w-16"></div>
              </div>
              <div className="h-8 bg-gray-200 rounded w-24"></div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (jobs.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-400 mb-4">
          <Search className="h-16 w-16 mx-auto" />
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No jobs found</h3>
        <p className="text-gray-500">Try adjusting your search criteria or filters.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {jobs.map((job) => (
        <JobCard 
          key={job.id} 
          job={job} 
          onClick={() => onJobClick?.(job)}
        />
      ))}
    </div>
  );
};

export default JobList;

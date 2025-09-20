// import api from '@/lib/api';
// import { Job, Proposal, PaginatedResponse } from '@/types/api';
// import axios from 'axios';

// // Job Proposal Service API instance
// const jobProposalAPI = axios.create({
//   baseURL: 'http://localhost:8080',
//   headers: {
//     'Content-Type': 'application/json',
//   },
//   timeout: 10000, // 10 second timeout
// });

// // Request interceptor for job proposal service
// // jobProposalAPI.interceptors.request.use(
// //   (config) => {
// //     if (typeof window !== 'undefined') {
// //       const token = localStorage.getItem('c-access-token');
// //       const userString = localStorage.getItem('user');
      
// //       console.log('Request interceptor - Token:', token ? 'Present' : 'Missing');
// //       console.log('Request interceptor - User data:', userString ? 'Present' : 'Missing');
      
// //       if (token) {
// //         config.headers.Authorization = token;
// //       }
      
// //       // Add user headers required by job-proposal-service
// //       if (userString) {
// //         try {
// //           const user = JSON.parse(userString);
// //           config.headers['X-User-Id'] = user.id?.toString() || '';
// //           config.headers['X-User-Email'] = user.email || '';
// //           config.headers['X-User-Role'] = user.role?.toUpperCase() || '';
// //           console.log('Request headers set:', {
// //             'X-User-Id': config.headers['X-User-Id'],
// //             'X-User-Email': config.headers['X-User-Email'],
// //             'X-User-Role': config.headers['X-User-Role']
// //           });
// //         } catch (error) {
// //           console.error('Error parsing user data from localStorage:', error);
// //         }
// //       }
      
// //       console.log('Final request config:', {
// //         url: config.url,
// //         headers: config.headers,
// //         params: config.params
// //       });
// //     }
// //     return config;
// //   },
// //   (error) => {
// //     return Promise.reject(error);
// //   }
// // );

// // // Response interceptor to log errors
// // jobProposalAPI.interceptors.response.use(
// //   (response) => {
// //     console.log('API Response:', response.status, response.config.url);
// //     return response;
// //   },
// //   (error) => {
// //     console.error('API Error:', error.response?.status, error.response?.data, error.config?.url);
// //     return Promise.reject(error);
// //   }
// // );

// jobProposalAPI.interceptors.request.use(
//   (config) => {
//     if (typeof window !== 'undefined') {
//       const token = localStorage.getItem('accessToken');
//       console.log(token);
//       // const userString = localStorage.getItem('user');

//       if (token) {
//         config.headers.Authorization = `Bearer ${token}`;
//       }

//       // if (userString) {
//       //   try {
//       //     const user = JSON.parse(userString);
//       //     config.headers['X-User-Id'] = user.id?.toString() || '';
//       //     config.headers['X-User-Email'] = user.email || '';
//       //     config.headers['X-User-Role'] = user.role?.toUpperCase() || '';
//       //   } catch (error) {
//       //     console.error('Invalid user data in localStorage');
//       //   }
//       // }
//     }
//     return config;
//   },
//   (error) => Promise.reject(error)
// );

// // Response Interceptor
// jobProposalAPI.interceptors.response.use(
//   (response) => response,
//   (error) => {
//     if (!error.response) {
//       console.error('Network/Server Error - Check API URL or CORS');
//     } else {
//       console.error(`API Error: ${error.response.status}`, error.response.data);
//     }
//     return Promise.reject(error);
//   }
// );

// export const jobAPI = {
//   // Get all jobs with pagination and filters
//   getAllJobs: async (params?: {
//     page?: number;
//     size?: number;
//     status?: string;
//     stack?: string[];
//     budget?: string;
//   }): Promise<PaginatedResponse<Job>> => {
//     const response = await jobProposalAPI.get('/api/jobs/search/with-client-info', { params });
//     return response.data;
//   },

//   // Get job by ID
//   getJobById: async (jobId: number): Promise<Job> => {
//     const response = await jobProposalAPI.get(`/api/jobs/${jobId}`);
//     return response.data;
//   },

//   // Get jobs by client ID (authenticated user's jobs)
//   getJobsByClientId: async (clientId: number): Promise<Job[]> => {
//     const response = await jobProposalAPI.get(`/api/jobs/user/${clientId}`);
//     return response.data;
//   },
  
//   // Get jobs posted by the current logged-in user
//   getMyJobs: async (): Promise<Job[]> => {
//     try {
//       // Accept status as parameter and use correct access token header
//       const status = 'DRAFT';
//       console.log('Making request to /api/jobs/my-jobs with status:', status);
//       // console.log('Auth token:', localStorage.getItem('c-access-token') ? 'Present' : 'Missing');
      
//       const response = await jobProposalAPI.get(`/api/jobs/my-jobs?status=${status}`);
//       console.log('Response received:', response.data);
//       return response.data;
//     } catch (error: any) {
//       console.error('Error in getMyJobs:', error);
//       console.error('Error response:', error.response?.data);
//       console.error('Error status:', error.response?.status);
//       throw error;
//     }
//   },

//   // Search jobs
//   searchJobs: async (params: {
//     query?: string;
//     stack?: string[];
//     budgetMin?: number;
//     budgetMax?: number;
//   }): Promise<Job[]> => {
//     const response = await jobProposalAPI.get('/api/jobs/search', { params });
//     return response.data;
//   },

//   // Create new job - Updated for job-proposal-service
//   createJob: async (jobData: {
//     projectName: string;
//     description: string;
//     category?: string;
//     skills?: string[];
//     isUrgent?: boolean;
//     budgetType: 'FIXED' | 'HOURLY';
//     minBudgetCents?: number;
//     maxBudgetCents?: number;
//     ndaRequired?: boolean;
//     ipAssignment?: boolean;
//   }): Promise<Job> => {
//     const response = await jobProposalAPI.post('/api/jobs', jobData);
//     return response.data;
//   },

//   // Update job
//   updateJob: async (jobId: number, jobData: Partial<Job>): Promise<Job> => {
//     const response = await jobProposalAPI.put(`/api/jobs/my-jobs/${jobId}`, jobData);
//     return response.data;
//   },

//   // Delete job
//   deleteJob: async (jobId: number): Promise<void> => {
//     await jobProposalAPI.delete(`/api/jobs/my-jobs/${jobId}`);
//   },
// };

// export const proposalAPI = {
//   // Submit proposal to job
//   submitProposal: async (jobId: number, proposalData: {
//     cover: string;
//     totalCents: number;
//     currency: string;
//     deliveryDays: number;
//   }): Promise<Proposal> => {
//     const response = await api.post(`/api/jobs/${jobId}/proposals`, proposalData);
//     return response.data;
//   },

//   // Get proposals for job
//   getProposalsForJob: async (jobId: number): Promise<Proposal[]> => {
//     const response = await api.get(`/api/jobs/${jobId}/proposals`);
//     return response.data;
//   },

//   // Get freelancer's proposals
//   getFreelancerProposals: async (): Promise<Proposal[]> => {
//     const response = await api.get('/api/proposals/me');
//     return response.data;
//   },

//   // Update proposal
//   updateProposal: async (proposalId: number, proposalData: Partial<Proposal>): Promise<Proposal> => {
//     const response = await api.put(`/api/proposals/${proposalId}`, proposalData);
//     return response.data;
//   },

//   // Withdraw proposal
//   withdrawProposal: async (proposalId: number): Promise<void> => {
//     await api.delete(`/api/proposals/${proposalId}`);
//   },
// };

// export default { jobAPI, proposalAPI };


import api from '@/lib/api';
import { Job, Proposal, PaginatedResponse, Attachment } from '@/types/api';
import { getApiUrl } from '@/config/api';
import axios from 'axios';

// Job Proposal Service API instance
const jobProposalAPI = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Request interceptor for job proposal service
// jobProposalAPI.interceptors.request.use(
//   (config) => {
//     if (typeof window !== 'undefined') {
//       const token = localStorage.getItem('c-access-token');
//       const userString = localStorage.getItem('user');
      
//       console.log('Request interceptor - Token:', token ? 'Present' : 'Missing');
//       console.log('Request interceptor - User data:', userString ? 'Present' : 'Missing');
      
//       if (token) {
//         config.headers.Authorization = token;
//       }
      
//       // Add user headers required by job-proposal-service
//       if (userString) {
//         try {
//           const user = JSON.parse(userString);
//           config.headers['X-User-Id'] = user.id?.toString() || '';
//           config.headers['X-User-Email'] = user.email || '';
//           config.headers['X-User-Role'] = user.role?.toUpperCase() || '';
//           console.log('Request headers set:', {
//             'X-User-Id': config.headers['X-User-Id'],
//             'X-User-Email': config.headers['X-User-Email'],
//             'X-User-Role': config.headers['X-User-Role']
//           });
//         } catch (error) {
//           console.error('Error parsing user data from localStorage:', error);
//         }
//       }
      
//       console.log('Final request config:', {
//         url: config.url,
//         headers: config.headers,
//         params: config.params
//       });
//     }
//     return config;
//   },
//   (error) => {
//     return Promise.reject(error);
//   }
// );

// // Response interceptor to log errors
// jobProposalAPI.interceptors.response.use(
//   (response) => {
//     console.log('API Response:', response.status, response.config.url);
//     return response;
//   },
//   (error) => {
//     console.error('API Error:', error.response?.status, error.response?.data, error.config?.url);
//     return Promise.reject(error);
//   }
// );

jobProposalAPI.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('accessToken');
      console.log(token);
      // const userString = localStorage.getItem('user');

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      // if (userString) {
      //   try {
      //     const user = JSON.parse(userString);
      //     config.headers['X-User-Id'] = user.id?.toString() || '';
      //     config.headers['X-User-Email'] = user.email || '';
      //     config.headers['X-User-Role'] = user.role?.toUpperCase() || '';
      //   } catch (error) {
      //     console.error('Invalid user data in localStorage');
      //   }
      // }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor
jobProposalAPI.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      console.error('Network/Server Error - Check API URL or CORS');
    } else {
      console.error(`API Error: ${error.response.status}`, error.response.data);
    }
    return Promise.reject(error);
  }
);

export const jobAPI = {
  // Get all jobs with pagination and filters
  getAllJobs: async (params?: {
    page?: number;
    size?: number;
    status?: string;
    stack?: string[];
    budget?: string;
  }): Promise<PaginatedResponse<Job>> => {
    const response = await jobProposalAPI.get('/api/jobs/search/with-client-info', { params });
    return response.data;
  },

  // Get job by ID
  getJobById: async (jobId: number): Promise<Job> => {
    const response = await jobProposalAPI.get(`/api/jobs/${jobId}`);
    return response.data;
  },

  getJobAttachments: async (jobId: number): Promise<Attachment[]> => {
    const response = await jobProposalAPI.get(`/api/jobs/${jobId}/attachments`);
    return response.data;
  },

  // Get jobs by client ID (authenticated user's jobs)
  getJobsByClientId: async (clientId: number): Promise<Job[]> => {
    const response = await jobProposalAPI.get(`/api/jobs/user/${clientId}`);
    return response.data;
  },
  
  // Get jobs posted by the current logged-in user
  getMyJobs: async (): Promise<Job[]> => {
    try {
      // Accept status as parameter and use correct access token header
      const status = 'DRAFT';
      console.log('Making request to /api/jobs/my-jobs with status:', status);
      // console.log('Auth token:', localStorage.getItem('c-access-token') ? 'Present' : 'Missing');
      
      const response = await jobProposalAPI.get(`/api/jobs/my-jobs?status=${status}`);
      console.log('Response received:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error in getMyJobs:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      throw error;
    }
  },

  // Search jobs
  searchJobs: async (params: {
    query?: string;
    stack?: string[];
    budgetMin?: number;
    budgetMax?: number;
  }): Promise<Job[]> => {
    const response = await jobProposalAPI.get('/api/jobs/search', { params });
    return response.data;
  },

  // Create new job - Updated for job-proposal-service
  createJob: async (jobData: {
    projectName: string;
    description: string;
    category?: string;
    skills?: string[];
    isUrgent?: boolean;
    budgetType: 'FIXED' | 'HOURLY';
    minBudgetCents?: number;
    maxBudgetCents?: number;
    ndaRequired?: boolean;
    ipAssignment?: boolean;
  } | FormData): Promise<Job> => {
    if (jobData instanceof FormData) {
      // For file uploads, create a dedicated axios instance to avoid header conflicts
      const token = localStorage.getItem('accessToken');
      const headers: any = {
        Authorization: `Bearer ${token}`,
        // Let axios set the 'Content-Type' for FormData automatically
      };

      const response = await axios.post(
        getApiUrl('/api/jobs/with-attachment'),
        jobData,
        { headers }
      );
      return response.data;
    } else {
      // Regular job creation without attachment
      const response = await jobProposalAPI.post('/api/jobs', jobData);
      return response.data;
    }
  },

  // Update job
  updateJob: async (jobId: number, jobData: Partial<Job>): Promise<Job> => {
    const response = await jobProposalAPI.put(`/api/jobs/my-jobs/${jobId}`, jobData);
    return response.data;
  },

  // Delete job
  deleteJob: async (jobId: number): Promise<void> => {
    await jobProposalAPI.delete(`/api/jobs/my-jobs/${jobId}`);
  },

  // Get matched freelancers for a job (Smart Matching)
  getMatchedFreelancers: async (jobId: number): Promise<any> => {
    const response = await jobProposalAPI.get(`/api/jobs/${jobId}/match-freelancers/smart`);
    return response.data;
  },
};

export const proposalAPI = {
  // Submit proposal to job
  submitProposal: async (jobId: number, proposalData: {
    cover: string;
    totalCents: number;
    currency: string;
    deliveryDays: number;
  }): Promise<Proposal> => {
    const response = await api.post(`/api/jobs/${jobId}/proposals`, proposalData);
    return response.data;
  },

  // Get proposals for job
  getProposalsForJob: async (jobId: number): Promise<Proposal[]> => {
    const response = await api.get(`/api/jobs/${jobId}/proposals`);
    return response.data;
  },

  // Get freelancer's proposals
  getFreelancerProposals: async (): Promise<Proposal[]> => {
    const response = await api.get('/api/proposals/me');
    return response.data;
  },

  // Update proposal
  updateProposal: async (proposalId: number, proposalData: Partial<Proposal>): Promise<Proposal> => {
    const response = await api.put(`/api/proposals/${proposalId}`, proposalData);
    return response.data;
  },

  // Withdraw proposal
  withdrawProposal: async (proposalId: number): Promise<void> => {
    await api.delete(`/api/proposals/${proposalId}`);
  },
};

export default { jobAPI, proposalAPI };

// // API Types for the freelancer platform
// export interface User {
//   id: number;
//   email: string;
//   name: string;
//   role: 'client' | 'freelancer' | 'admin';
//   handle?: string;
//   country?: string;
//   timezone?: string;
//   kycStatus?: string;
//   isActive: boolean;
//   createdAt: string;
//   updatedAt: string;
//   profilePicture?: string; // Optional profile picture property
//   profilePictureUrl?: string; // Alternative field name used by some APIs
//   stripeAccountId?: string | null; // Stripe account ID for payments
// }

// export interface LoginRequest {
//   email: string;
//   password: string;
// }

// export interface RegisterRequest {
//   email: string;
//   password: string;
//   name: string;
//   role: 'client' | 'freelancer';
// }

// export interface LoginResponse {
//   accessToken: string;
//   refreshToken: string;
//   user: User;
// }

// export interface Profile {
//   userId: number;
//   headline?: string;
//   bio?: string;
//   hourlyRateCents?: number;
//   currency?: string;
//   availability: 'FULL_TIME' | 'PART_TIME' | 'OCCASIONAL' | 'UNAVAILABLE';
//   languages?: string[];
//   skills?: string[];
//   locationText?: string;
//   githubUsername?: string;
//   gitlabUsername?: string;
//   websiteUrl?: string;
//   linkedinUrl?: string;
//   deliveryScore: number;
//   reviewAvg: number;
//   reviewsCount: number;
//   createdAt: string;
//   updatedAt: string;
// }

// export interface Contract {
//   id: number;
//   jobId: number;
//   jobTitle: string;
//   proposalId: number;
//   clientId: number;
//   freelancerId: number;
//   totalAmountCents: number;
//   currency: string;
//   paymentModel: 'FIXED' | 'HOURLY';
//   status: 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'TERMINATED';
//   startDate: string;
//   endDate: string;
//   termsJson: string;
//   totalMilestones: number;
//   completedMilestones: number;
//   activeMilestones: number;
//   milestones: any[];
//   createdAt: string;
//   updatedAt: string;
// }

// export interface Gig {
//   id: number;
//   title: string;
//   description: string;
//   category: string;
//   tags: string[];
//   skills?: string[];
//   price: number;
//   startingPrice?: number;
//   delivery_time: number;
//   deliveryDays?: number;
//   freelancer_id: number;
//   freelancerInfo?: {
//     id: number;
//     name: string;
//     profilePictureUrl?: string;
//   };
//   rating?: number;
//   active: boolean;
//   created_at: Date;
//   updated_at: Date;
// }

// export interface GigPackage {
//   id: number;
//   gigId: number;
//   tier: 'BASIC' | 'STANDARD' | 'PREMIUM';
//   title: string;
//   description: string;
//   priceCents: number;
//   currency: string;
//   deliveryDays: number;
//   revisions?: number;
//   createdAt: string;
//   updatedAt: string;
// }

// export interface Job {
//   id: number;
//   clientId: number;
//   projectName: string;
//   description: string;
//   category?: string;
//   skills?: string[];
//   isUrgent?: boolean;
//   budgetType: 'FIXED' | 'HOURLY';
//   minBudgetCents?: number;
//   maxBudgetCents?: number;
//   ndaRequired?: boolean;
//   ipAssignment?: boolean;
//   status: 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
//   currency: string;
//   paymentModel: 'FIXED' | 'HOURLY';
//   createdAt: string;
//   updatedAt: string;
//   editedAt?: string;
//   // Legacy fields for backward compatibility
//   title?: string;
//   budget?: number;
//   budget_min?: number;
//   budget_max?: number;
//   deadline?: Date | string;
//   client?: {
//     id: number;
//     name: string;
//     email?: string;
//   };
//   created_at?: Date;
// }

// export interface Proposal {
//   id: number;
//   jobId: number;
//   freelancerId: number;
//   cover: string;
//   proposedRateCents: number;
//   currency: string;
//   deliveryDays: number;
//   status: 'SUBMITTED' | 'WITHDRAWN' | 'DECLINED' | 'ACCEPTED';
//   freelancerInfo?: {
//     name: string;
//     profile?: {
//       headline: string;
//       reviewAvg: number;
//       reviewsCount: number;
//       deliveryScore: number;
//     }
//   };
//   createdAt: string;
//   updatedAt: string;
// }

// export interface ApiResponse<T> {
//   data: T;
//   message?: string;
//   success: boolean;
// }

// export interface PaginatedResponse<T> {
//   content: T[];
//   totalElements: number;
//   totalPages: number;
//   size: number;
//   number: number;
//   first: boolean;
//   last: boolean;
// }

// // Chat System Types
// export interface Message {
//   id: number | string; // Allow string for temporary IDs
//   conversationId: number;
//   senderId: number;
//   content: string;
//   messageType: 'TEXT' | 'IMAGE' | 'FILE';
//   isRead: boolean;
//   createdAt: string;
//   updatedAt: string;
//   sender?: User;
// }

// export interface Conversation {
//   id: number;
//   participantIds: number[];
//   participants: User[];
//   lastMessage?: Message;
//   unreadCount?: number;
//   createdAt: string;
//   updatedAt: string;
// }

// // For conversations based on direct messages
// export interface ChatConversation {
//   otherUser: User;
//   lastMessageTime: string;
//   lastMessageContent?: string;
//   unreadCount?: number;
// }

// export interface SendMessageRequest {
//   senderId: string;
//   receiverId: string;
//   content: string;
//   messageType: 'TEXT' | 'IMAGE' | 'FILE';
//   replyToId?: string;
//   attachments?: {
//     filename: string;
//     url: string;
//     contentType: string;
//     fileSize: number;
//   }[];
// }

// export interface CreateConversationRequest {
//   participantIds: number[];
//   initialMessage?: string;
// }

// export interface Event {
//   id: number;
//   roomId: number;
//   title: string;
//   description?: string;
//   eventType: 'MEETING' | 'DEADLINE' | 'MILESTONE' | 'REVIEW';
//   startTime: string;
//   endTime: string;
//   createdAt?: string;
//   updatedAt?: string;
// }

// export interface CreateEventRequest {
//   roomId: number;
//   title: string;
//   description?: string;
//   eventType: 'MEETING' | 'DEADLINE' | 'MILESTONE' | 'REVIEW';
//   startTime: string;
//   endTime: string;
// }


// API Types for the freelancer platform
export interface User {
  id: number;
  email: string;
  name: string;
  role: 'client' | 'freelancer' | 'admin';
  handle?: string;
  country?: string;
  timezone?: string;
  kycStatus?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  profilePicture?: string; // Optional profile picture property
  profilePictureUrl?: string; // Profile picture URL from API
  stripeAccountId?: string | null; // Stripe account ID for payments
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  role: 'client' | 'freelancer';
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface Profile {
  userId: number;
  headline?: string;
  bio?: string;
  hourlyRateCents?: number;
  currency?: string;
  availability: 'FULL_TIME' | 'PART_TIME' | 'OCCASIONAL' | 'UNAVAILABLE';
  languages?: string[];
  skills?: string[];
  locationText?: string;
  githubUsername?: string;
  gitlabUsername?: string;
  websiteUrl?: string;
  linkedinUrl?: string;
  deliveryScore: number;
  reviewAvg: number;
  reviewsCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Contract {
  id: number;
  jobId: number;
  jobTitle: string;
  proposalId: number;
  clientId: number;
  freelancerId: number;
  totalAmountCents: number;
  currency: string;
  paymentModel: 'FIXED' | 'HOURLY';
  status: 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'TERMINATED';
  startDate: string;
  endDate: string;
  termsJson: string;
  totalMilestones: number;
  completedMilestones: number;
  activeMilestones: number;
  milestones: any[];
  createdAt: string;
  updatedAt: string;
}

export interface Gig {
  id: number;
  title: string;
  description: string;
  category: string;
  tags: string[];
  skills?: string[];
  price: number;
  startingPrice?: number;
  delivery_time: number;
  deliveryDays?: number;
  freelancer_id: number;
  freelancerInfo?: {
    id: number;
    name: string;
    profilePictureUrl?: string;
  };
  rating?: number;
  active: boolean;
  created_at: Date;
  updated_at: Date;
}

export interface GigPackage {
  id: number;
  gigId: number;
  tier: 'BASIC' | 'STANDARD' | 'PREMIUM';
  title: string;
  description: string;
  priceCents: number;
  currency: string;
  deliveryDays: number;
  revisions?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Job {
  id: number;
  clientId: number;
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
  status: 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  currency: string;
  paymentModel: 'FIXED' | 'HOURLY';
  createdAt: string;
  updatedAt: string;
  editedAt?: string;
  // Legacy fields for backward compatibility
  title?: string;
  budget?: number;
  budget_min?: number;
  budget_max?: number;
  deadline?: Date | string;
  client?: {
    id: number;
    name: string;
    email?: string;
  };
  created_at?: Date;
}

export interface Proposal {
  id: number;
  jobId: number;
  freelancerId: number;
  cover: string;
  proposedRateCents: number;
  currency: string;
  deliveryDays: number;
  status: 'SUBMITTED' | 'WITHDRAWN' | 'DECLINED' | 'ACCEPTED';
  freelancerInfo?: {
    name: string;
    profile?: {
      headline: string;
      reviewAvg: number;
      reviewsCount: number;
      deliveryScore: number;
    }
  };
  createdAt: string;
  updatedAt: string;
}

export interface Attachment {
  id: number;
  jobId: number;
  kind: string | null;
  url: string;
  filename: string | null;
  bytes: number;
  contentType: string;
  cloudinaryPublicId: string;
  cloudinaryResourceType: string;
  checksum: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Chat System Types
export interface Message {
  id: number | string; // Allow string for temporary IDs
  conversationId: number;
  senderId: number;
  content: string;
  messageType: 'TEXT' | 'IMAGE' | 'FILE';
  isRead: boolean;
  createdAt: string;
  updatedAt: string;
  sender?: User;
  isDeleted?: boolean; // Optional flag to track deleted messages
}

export interface Conversation {
  id: number;
  participantIds: number[];
  participants: User[];
  lastMessage?: Message;
  unreadCount?: number;
  createdAt: string;
  updatedAt: string;
}

// For conversations based on direct messages
export interface ChatConversation {
  otherUser: User;
  lastMessageTime: string;
  lastMessageContent?: string;
  unreadCount?: number;
}

export interface SendMessageRequest {
  senderId: string;
  receiverId: string;
  content: string;
  messageType: 'TEXT' | 'IMAGE' | 'FILE';
  replyToId?: string;
  attachments?: {
    filename: string;
    url: string;
    contentType: string;
    fileSize: number;
  }[];
}

export interface CreateConversationRequest {
  participantIds: number[];
  initialMessage?: string;
}

export interface Event {
  id: number;
  roomId: number;
  title: string;
  description?: string;
  eventType: 'MEETING' | 'DEADLINE' | 'MILESTONE' | 'REVIEW';
  startTime: string;
  endTime: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateEventRequest {
  roomId: number;
  title: string;
  description?: string;
  eventType: 'MEETING' | 'DEADLINE' | 'MILESTONE' | 'REVIEW';
  startTime: string;
  endTime: string;
}

// Task System Types
export interface Task {
  id: number;
  roomId: number;
  title: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
  assigneeId: number;
  assignee?: User;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskCreate {
  title: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  assigneeId: number;
  dueDate?: string;
}

export interface TaskUpdate {
  title?: string;
  description?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  status?: 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
  assigneeId?: number;
  dueDate?: string;
}

export interface TaskListResponse {
  tasks: Task[];
  totalCount: number;
}

// Contract Submission System Types
export interface ContractSubmission {
  id: number;
  contractId: number;
  jobTitle: string;
  clientId: number;
  freelancerId: number;
  description: string;
  deliverableUrls: string[];
  notes?: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  submittedAt: string;
  acceptedAt?: string;
  rejectedAt?: string;
  feedback?: string;
  submissionDescription: string;
  submissionNotes?: string;
  rejectionReason?: string;
  rejectionFeedback?: string;
  contractStatus: string;
  isSubmitted: boolean;
  isAccepted: boolean;
  isRejected: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ContractSubmissionRequest {
  description: string;
  deliverableUrls: string[];
  notes?: string;
}

export interface ContractRejectRequest {
  reason: string;
  feedback: string;
  pauseContract?: boolean;
}

export interface ContractAcceptRequest {
  feedback?: string; // Optional feedback when accepting
}

// Matched Freelancer Interface for Smart Matching
export interface MatchedFreelancer {
  userId: number;
  headline: string;
  bio: string;
  hourlyRateUsd: number;
  matchScore: number;
  skillMatches: string[];
  reviewAvg: number;
  reviewsCount: number;
  deliveryScore: number;
  githubUsername?: string;
  websiteUrl?: string;
  profilePictureUrl?: string;
  matchReason?: string;
  skills?: string[];
}

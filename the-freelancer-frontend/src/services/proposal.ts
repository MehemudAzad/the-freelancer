import api from '@/lib/api';

export interface ProposalData {
  jobId: number;
  coverLetter: string;
  proposedRate: number;
  deliveryDays: number;
  portfolioLinks?: string; // Changed from string[] to string
  additionalNotes?: string;
}

export interface Proposal {
  id: number;
  jobId: number;
  jobTitle?: string; // Optional job title for display
  freelancerId: number;
  coverLetter: string;
  proposedRate: number;
  deliveryDays: number;
  portfolioLinks: string; // Changed from string[] to string
  additionalNotes?: string;
  status: 'SUBMITTED' | 'PENDING' | 'DECLINED' | 'ACCEPTED' | 'REJECTED' | 'CONTRACTED';
  createdAt: string;
  updatedAt: string;
  contractId?: number; // Populated when proposal is accepted and contract is created   
  // Freelancer information (populated by backend)
  freelancerInfo?: {
    id: number;
    name: string;
    email: string;
    profilePictureUrl?: string;
    joinedAt: string;   
  };
}

class ProposalService {
  // Submit a new proposal for a job
  async submitProposal(proposalData: ProposalData): Promise<Proposal> {
    const response = await api.post('/api/proposals/my-proposals', proposalData);
    return response.data;
  }

  // Get all proposals submitted by the current freelancer
  async getMyProposals(): Promise<Proposal[]> {
    const response = await api.get('/api/proposals/my-proposals');
    return response.data;
  }

  // Get all proposals for a specific job (for clients)
  async getJobProposals(jobId: number): Promise<Proposal[]> {
    const response = await api.get(`/api/proposals/job/${jobId}`);
    return response.data;
  }

  // Get a specific proposal by ID
  async getProposal(id: number): Promise<Proposal> {
    const response = await api.get(`/api/proposals/${id}`);
    return response.data;
  }

  // Update a proposal (if still pending)
  async updateProposal(id: number, proposalData: Partial<ProposalData>): Promise<Proposal> {
    const response = await api.put(`/api/proposals/${id}`, proposalData);
    return response.data;
  }

  // Withdraw a proposal
  async withdrawProposal(id: number): Promise<void> {
    await api.delete(`/api/proposals/${id}`);
  }

  // Accept a proposal (for clients)
//   async acceptProposal(id: number): Promise<Proposal> {
//     const response = await api.post(`/api/proposals/${id}/accept`);
//     return response.data;
//   }

  // Reject a proposal (for clients)
  async rejectProposal(id: number): Promise<Proposal> {
    const response = await api.post(`/api/proposals/${id}/reject`);
    return response.data;
  }
}

const proposalService = new ProposalService();
export default proposalService;

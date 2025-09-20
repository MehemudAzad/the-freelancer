import api from '@/lib/api';

export interface InviteRequest {
  jobId: number;
  clientId?: number;  // Optional, can be derived from token
  freelancerId: number;
  message?: string;   // Optional invitation message
}

export interface Invite {
  id: number;
  jobId: number;
  clientId: number;
  freelancerId: number;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED';
  createdAt: string;
  updatedAt: string;
}

class InviteService {
  // Create a new job invitation
  async createInvite(inviteData: InviteRequest): Promise<Invite> {
    const response = await api.post('/api/invites', inviteData);
    return response.data;
  }

  // Get invites for a specific job
  async getJobInvites(jobId: number): Promise<Invite[]> {
    const response = await api.get(`/api/invites/job/${jobId}`);
    return response.data;
  }

  // Get invites for a specific freelancer
  async getFreelancerInvites(freelancerId: number): Promise<Invite[]> {
    const response = await api.get(`/api/invites/freelancer/${freelancerId}`);
    return response.data;
  }

  // Get invites sent by a client
  async getClientInvites(clientId: number): Promise<Invite[]> {
    const response = await api.get(`/api/invites/client/${clientId}`);
    return response.data;
  }

  // Update invite status (accept/decline)
  async updateInviteStatus(inviteId: number, status: 'ACCEPTED' | 'DECLINED'): Promise<Invite> {
    const response = await api.put(`/api/invites/${inviteId}/status`, { status });
    return response.data;
  }

  // Get invitations received by the current user (freelancer)
  async getMyReceivedInvites(): Promise<any[]> {
    const response = await api.get('/api/invites/my-received');
    return response.data;
  }
}

const inviteService = new InviteService();
export default inviteService;
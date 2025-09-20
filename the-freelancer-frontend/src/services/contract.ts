import api from '@/lib/api';
import { Contract, ContractSubmission, ContractSubmissionRequest, ContractRejectRequest, ContractAcceptRequest } from '@/types/api';
import userService from './user';

const createContract = async (contractData: any): Promise<Contract> => {
  const { data } = await api.post('/api/contracts', contractData);
  return data;
};

const getContractById = async (contractId: string): Promise<Contract> => {
  const { data } = await api.get(`/api/contracts/${contractId}`);
  return data;
};

const getMyContracts = async (): Promise<Contract[]> => {
  const { data } = await api.get('/api/contracts/my-contracts');
  return data;
};

// Contract submission methods
const submitContract = async (contractId: string, submissionData: ContractSubmissionRequest): Promise<ContractSubmission> => {
  const { data } = await api.post(`/api/contracts/${contractId}/submit`, submissionData);
  return data;
};

const acceptSubmission = async (contractId: string, acceptData?: ContractAcceptRequest): Promise<Contract> => {
  const { data } = await api.post(`/api/contracts/${contractId}/accept`, acceptData || {});
  return data;
};

const rejectSubmission = async (contractId: string, rejectData: ContractRejectRequest): Promise<Contract> => {
  const { data } = await api.post(`/api/contracts/${contractId}/reject`, rejectData);
  return data;
};

const getContractSubmission = async (contractId: string): Promise<ContractSubmission> => {
  const { data } = await api.get(`/api/contracts/${contractId}/submission`);
  return data;
};

// New method to release escrow money
const releaseEscrowMoney = async (contractId: string): Promise<void> => {
  try {
    // Fetch contract details to get jobId
    const contract = await getContractById(contractId);
    const jobId = contract.jobId;

    // Fetch submission details to get freelancerId
    const submission = await getContractSubmission(contractId);
    const freelancerId = submission.freelancerId;

    // Fetch freelancer details to get stripeAccountId
    const freelancer = await userService.getUserById(freelancerId);
    const stripeAccountId = freelancer.stripeAccountId;

    if (!stripeAccountId) {
      throw new Error('Freelancer does not have a Stripe account ID');
    }

    // Call the escrow release API
    const releaseUrl = `/api/payments/escrow/job/${jobId}/release?destinationAccountId=${stripeAccountId}`;
    await api.post(releaseUrl);

    console.log('Escrow money released successfully');
  } catch (error) {
    console.error('Failed to release escrow money:', error);
    throw error;
  }
};

const contractService = {
  createContract,
  getContractById,
  getMyContracts,
  submitContract,
  acceptSubmission,
  rejectSubmission,
  getContractSubmission,
  releaseEscrowMoney,
};

export default contractService;

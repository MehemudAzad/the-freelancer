import api from '@/lib/api';

export interface Workspace {
  id: string;
  contractId: number;
  roomId: string;
  // Add other workspace properties as needed
}

const getWorkspaceByContractId = async (contractId: string): Promise<Workspace> => {
  const { data } = await api.get(`/api/workspaces/contract/${contractId}`);
  return data;
};

const workspaceService = {
    getWorkspaceByContractId,
};

export default workspaceService;

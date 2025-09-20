import api from '@/lib/api';
import { Task, TaskCreate, TaskUpdate, TaskListResponse } from '@/types/api';

const taskService = {
  getTasksByRoomId: async (roomId: string): Promise<TaskListResponse> => {
    const response = await api.get(`/api/workspaces/rooms/${roomId}/tasks`);
    return response.data;
  },

  createTask: async (roomId: string, taskData: TaskCreate): Promise<Task> => {
    const response = await api.post(`/api/workspaces/rooms/${roomId}/tasks`, taskData);
    return response.data;
  },

  updateTask: async (taskId: number, taskData: TaskUpdate): Promise<Task> => {
    const response = await api.patch(`/api/workspaces/tasks/${taskId}`, taskData);
    return response.data;
  },

  updateTaskStatus: async (roomId: string, taskId: number, status: 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE'): Promise<Task> => {
    const response = await api.patch(`/api/workspaces/rooms/${roomId}/tasks/${taskId}/status`, { status });
    return response.data;
  },

  deleteTask: async (roomId: string, taskId: number): Promise<void> => {
    await api.delete(`/api/workspaces/rooms/${roomId}/tasks/${taskId}`);
  },
};

export default taskService;

import api from '@/lib/api';
import { Event, CreateEventRequest } from '@/types/api';

const createEvent = async (eventData: CreateEventRequest): Promise<Event> => {
  const { data } = await api.post('/api/workspaces/events', eventData);
  return data;
};

const getEventsByRoomId = async (roomId: string): Promise<Event[]> => {
  const { data } = await api.get(`/api/workspaces/events/room/${roomId}`);
  return data;
};

const eventService = {
  createEvent,
  getEventsByRoomId,
};

export default eventService;
export type { Event };

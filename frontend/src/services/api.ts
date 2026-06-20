import axios from 'axios';
import type { Meeting, TranscriptTurn, Summary, ActionItem, SearchResponse, ActionStatus } from '../types';

const api = axios.create({ baseURL: '/api' });

// Meetings
export const createMeeting    = (title: string) =>
  api.post<Meeting>('/meetings', { title }).then(r => r.data);

export const getMeetings      = () =>
  api.get<Meeting[]>('/meetings').then(r => r.data);

export const getMeeting       = (id: string) =>
  api.get<Meeting>(`/meetings/${id}`).then(r => r.data);

export const endMeeting       = (id: string) =>
  api.post<Meeting>(`/meetings/${id}/end`).then(r => r.data);

export const getSummary       = (id: string) =>
  api.get<Summary>(`/meetings/${id}/summary`).then(r => r.data);

export const getActionItems   = (id: string) =>
  api.get<ActionItem[]>(`/meetings/${id}/tasks`).then(r => r.data);

export const updateTaskStatus = (taskId: string, status: ActionStatus) =>
  api.patch<ActionItem>(`/meetings/tasks/${taskId}`, { status }).then(r => r.data);

// Transcripts
export const getTranscripts   = (meetingId: string) =>
  api.get<TranscriptTurn[]>(`/meetings/${meetingId}/transcripts`).then(r => r.data);

// Search
export const searchAll        = (q: string) =>
  api.get<SearchResponse>('/search', { params: { q } }).then(r => r.data);

export const searchInMeeting  = (meetingId: string, q: string) =>
  api.get<SearchResponse>(`/search/meetings/${meetingId}`, { params: { q } }).then(r => r.data);

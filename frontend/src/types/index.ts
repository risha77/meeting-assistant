export type MeetingStatus = 'IN_PROGRESS' | 'ENDED' | 'PROCESSING' | 'COMPLETED';
export type ActionStatus  = 'PENDING' | 'IN_PROGRESS' | 'DONE';

export interface Meeting {
  id: string;
  title: string;
  startedAt: string;
  endedAt?: string;
  status: MeetingStatus;
}

export interface TranscriptTurn {
  id: string;
  meetingId: string;
  speaker: string;
  content: string;
  ts: string;
}

export interface Summary {
  id: string;
  meetingId: string;
  summary: string;
  decisions: string;
  risks: string;
  topics: string;
  createdAt: string;
}

export interface ActionItem {
  id: string;
  meetingId: string;
  assignee: string;
  task: string;
  deadline?: string;
  status: ActionStatus;
}

export interface TranscriptSearchResult {
  id: string;
  meetingId: string;
  meetingTitle: string;
  speaker: string;
  content: string;
  ts: string;
}

export interface SearchResponse {
  query: string;
  total: number;
  results: TranscriptSearchResult[];
}

// WebSocket messages from backend
export interface WsTranscriptMessage {
  type: 'transcript' | 'error' | 'connected';
  speaker?: string;
  content: string;
  isFinal?: boolean;
  timestamp?: string;
}

// Live (interim) transcript turn shown in UI before persisted
export interface LiveTurn {
  speaker: string;
  content: string;
  isFinal: boolean;
  timestamp: string;
}

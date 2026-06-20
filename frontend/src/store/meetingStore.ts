/**
 * meetingStore.ts
 *
 * Lightweight Zustand store for cross-component meeting state.
 * The main MeetingPage manages most state locally via useState,
 * but this store is useful if you later add components that need
 * access to the active meeting without prop drilling.
 *
 * Install: npm install zustand
 */
import { create } from 'zustand';
import type { Meeting } from '../types';

interface MeetingStore {
  activeMeeting: Meeting | null;
  setActiveMeeting: (meeting: Meeting | null) => void;

  sidebarOpen: boolean;
  toggleSidebar: () => void;
}

export const useMeetingStore = create<MeetingStore>((set) => ({
  activeMeeting: null,
  setActiveMeeting: (meeting) => set({ activeMeeting: meeting }),

  sidebarOpen: true,
  toggleSidebar: () => set((s) => ({ sidebarOpen: !s.sidebarOpen })),
}));

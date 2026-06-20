import { useState, useEffect, useCallback } from 'react';
import clsx from 'clsx';
import type { Meeting, TranscriptTurn, LiveTurn } from '../types';
import { Sidebar }          from '../components/Sidebar';
import { LiveTranscript }   from '../components/LiveTranscript';
import { SummaryPanel }     from '../components/SummaryPanel';
import { ActionItemsPanel } from '../components/ActionItemsPanel';
import { SearchPanel }      from '../components/SearchPanel';
import { NewMeetingModal }  from '../components/NewMeetingModal';
import { useAudioRecorder } from '../hooks/useAudioRecorder';
import { useTimer }         from '../hooks/useTimer';
import {
  getMeetings, createMeeting, endMeeting, getTranscripts,
} from '../services/api';

type Tab = 'live' | 'summary' | 'actions' | 'search';

export function MeetingPage() {
  const [meetings, setMeetings]       = useState<Meeting[]>([]);
  const [selectedId, setSelectedId]   = useState<string | null>(null);
  const [persisted, setPersisted]     = useState<TranscriptTurn[]>([]);
  const [live, setLive]               = useState<LiveTurn[]>([]);
  const [tab, setTab]                 = useState<Tab>('live');
  const [showModal, setShowModal]     = useState(false);
  const [error, setError]             = useState('');

  const selectedMeeting = meetings.find(m => m.id === selectedId) ?? null;

  // Load meetings on mount
  useEffect(() => {
    getMeetings().then(setMeetings).catch(() => {});
  }, []);

  // Load transcript when meeting changes
  useEffect(() => {
    setPersisted([]);
    setLive([]);
    if (selectedId) {
      getTranscripts(selectedId).then(setPersisted).catch(() => {});
    }
  }, [selectedId]);

  const handleTurn = useCallback((turn: LiveTurn) => {
    setLive(prev => {
      const last = prev[prev.length - 1];
      if (last && !last.isFinal && last.speaker === turn.speaker) {
        return [...prev.slice(0, -1), turn];
      }
      return [...prev, turn];
    });
  }, []);

  const { recording, start, stop } = useAudioRecorder({
    meetingId: selectedId ?? '',
    onTurn: handleTurn,
    onError: setError,
  });

  const { display: timer } = useTimer(recording);

  const handleNewMeeting = async (title: string) => {
    try {
      const m = await createMeeting(title);
      setMeetings(prev => [m, ...prev]);
      setSelectedId(m.id);
      setTab('live');
      setShowModal(false);
    } catch {
      setError('Failed to create meeting');
    }
  };

  const handleStop = async () => {
    stop();
    if (selectedId) {
      try {
        const updated = await endMeeting(selectedId);
        setMeetings(prev => prev.map(m => m.id === updated.id ? updated : m));
      } catch {
        setError('Failed to end meeting on server');
      }
    }
  };

  const TABS: { key: Tab; label: string }[] = [
    { key: 'live',    label: 'Live transcript' },
    { key: 'summary', label: 'AI summary' },
    { key: 'actions', label: 'Action items' },
    { key: 'search',  label: 'Search' },
  ];

  return (
    <div className="flex h-screen bg-gray-50 text-gray-900 overflow-hidden">
      <Sidebar
        meetings={meetings}
        selectedId={selectedId}
        onSelect={setSelectedId}
        onNew={() => setShowModal(true)}
      />

      <main className="flex-1 flex flex-col min-w-0 bg-white">
        {/* Tab bar */}
        <div className="flex border-b border-gray-100 px-4">
          {TABS.map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={clsx(
                'text-sm px-4 py-3 border-b-2 transition-colors whitespace-nowrap',
                tab === t.key
                  ? 'border-brand-500 text-brand-500 font-medium'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              )}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Error banner */}
        {error && (
          <div className="bg-red-50 border-b border-red-100 px-4 py-2 text-sm text-red-600 flex items-center justify-between">
            {error}
            <button onClick={() => setError('')} className="text-red-400 hover:text-red-600 ml-2">✕</button>
          </div>
        )}

        {/* Panel */}
        <div className="flex-1 min-h-0 overflow-hidden">
          {tab === 'search' ? (
            <SearchPanel />
          ) : !selectedMeeting ? (
            <div className="flex flex-col items-center justify-center h-full gap-3">
              <p className="text-sm text-gray-400">Select a meeting or create a new one.</p>
              <button
                onClick={() => setShowModal(true)}
                className="text-sm bg-brand-500 text-white px-4 py-2 rounded-lg hover:bg-brand-600 transition-colors"
              >
                + New meeting
              </button>
            </div>
          ) : (
            <>
              {tab === 'live' && (
                <LiveTranscript
                  persisted={persisted}
                  live={live}
                  recording={recording}
                  timer={timer}
                  onStart={start}
                  onStop={handleStop}
                />
              )}
              {tab === 'summary' && <SummaryPanel meeting={selectedMeeting} />}
              {tab === 'actions' && <ActionItemsPanel meeting={selectedMeeting} />}
            </>
          )}
        </div>
      </main>

      {showModal && (
        <NewMeetingModal
          onConfirm={handleNewMeeting}
          onClose={() => setShowModal(false)}
        />
      )}
    </div>
  );
}

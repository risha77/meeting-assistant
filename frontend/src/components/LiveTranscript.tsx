import { useEffect, useRef } from 'react';
import { format } from 'date-fns';
import type { TranscriptTurn, LiveTurn } from '../types';
import clsx from 'clsx';

interface Props {
  persisted: TranscriptTurn[];
  live: LiveTurn[];
  recording: boolean;
  timer: string;
  onStart: () => void;
  onStop: () => void;
}

const SPEAKER_COLORS: Record<string, string> = {
  'Speaker 1': 'bg-brand-50 text-brand-500',
  'Speaker 2': 'bg-emerald-50 text-emerald-700',
  'Speaker 3': 'bg-orange-50 text-orange-700',
  'Speaker 4': 'bg-pink-50 text-pink-700',
};

function avatarInitials(speaker: string) {
  const parts = speaker.split(' ');
  return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2);
}

export function LiveTranscript({ persisted, live, recording, timer, onStart, onStop }: Props) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [persisted.length, live.length]);

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-3 border-b border-gray-100">
        <div className="flex items-center gap-2">
          {recording && <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />}
          <span className="text-sm text-gray-500">
            {recording ? 'Live · Deepgram STT' : 'Transcript'}
          </span>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-sm font-mono text-gray-500">{timer}</span>
          <button
            onClick={recording ? onStop : onStart}
            className={clsx(
              'text-sm font-medium px-3 py-1.5 rounded-lg transition-colors',
              recording
                ? 'bg-red-500 hover:bg-red-600 text-white'
                : 'bg-brand-500 hover:bg-brand-600 text-white'
            )}
          >
            {recording ? '⏹ Stop' : '⏺ Start meeting'}
          </button>
        </div>
      </div>

      {/* Turns */}
      <div className="flex-1 overflow-y-auto p-5 space-y-4">
        {persisted.map(turn => (
          <Turn
            key={turn.id}
            speaker={turn.speaker ?? 'Speaker 1'}
            content={turn.content}
            ts={format(new Date(turn.ts), 'HH:mm:ss')}
            isFinal
          />
        ))}

        {live.map((turn, i) => (
          <Turn
            key={`live-${i}`}
            speaker={turn.speaker}
            content={turn.content}
            ts={format(new Date(turn.timestamp), 'HH:mm:ss')}
            isFinal={turn.isFinal}
            isLive
          />
        ))}

        {persisted.length === 0 && live.length === 0 && (
          <p className="text-sm text-gray-400 text-center mt-16">
            {recording ? 'Listening…' : 'Start the meeting to begin transcription.'}
          </p>
        )}

        <div ref={bottomRef} />
      </div>
    </div>
  );
}

function Turn({ speaker, content, ts, isFinal, isLive }: {
  speaker: string; content: string; ts: string; isFinal: boolean; isLive?: boolean;
}) {
  const colorClass = SPEAKER_COLORS[speaker] ?? 'bg-gray-100 text-gray-600';

  return (
    <div className="flex gap-3">
      <div className={clsx('w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0 mt-0.5', colorClass)}>
        {avatarInitials(speaker)}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <span className="text-xs font-semibold text-gray-600">{speaker}</span>
          <span className="text-xs text-gray-400">{ts}</span>
          {isLive && !isFinal && (
            <span className="text-xs text-brand-500 font-medium">typing…</span>
          )}
        </div>
        <p className={clsx('text-sm text-gray-800 leading-relaxed', !isFinal && 'opacity-60')}>
          {content}
          {isLive && !isFinal && (
            <span className="inline-block w-0.5 h-3.5 bg-brand-500 ml-0.5 align-middle animate-pulse" />
          )}
        </p>
      </div>
    </div>
  );
}

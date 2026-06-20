import { Mic } from 'lucide-react';
import { format } from 'date-fns';
import type { Meeting } from '../types';
import clsx from 'clsx';

interface Props {
  meetings: Meeting[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  onNew: () => void;
}

export function Sidebar({ meetings, selectedId, onSelect, onNew }: Props) {
  const inProgress  = meetings.filter(m => m.status === 'IN_PROGRESS');
  const completed   = meetings.filter(m => m.status !== 'IN_PROGRESS');

  return (
    <aside className="w-60 flex-shrink-0 bg-white border-r border-gray-100 flex flex-col h-full">
      {/* Brand */}
      <div className="h-12 flex items-center px-4 border-b border-gray-100 gap-2">
        <Mic size={16} className="text-brand-500" />
        <span className="text-sm font-semibold text-gray-800">Synrixa Meetings</span>
      </div>

      {/* New meeting btn */}
      <div className="p-3">
        <button
          onClick={onNew}
          className="w-full bg-brand-500 hover:bg-brand-600 text-white text-sm font-medium rounded-lg py-2 transition-colors"
        >
          + New meeting
        </button>
      </div>

      <div className="flex-1 overflow-y-auto">
        {inProgress.length > 0 && (
          <>
            <p className="px-4 pt-2 pb-1 text-[10px] font-medium uppercase tracking-widest text-gray-400">Live</p>
            {inProgress.map(m => (
              <MeetingRow key={m.id} meeting={m} active={m.id === selectedId} onClick={() => onSelect(m.id)} />
            ))}
          </>
        )}

        {completed.length > 0 && (
          <>
            <p className="px-4 pt-4 pb-1 text-[10px] font-medium uppercase tracking-widest text-gray-400">Recent</p>
            {completed.map(m => (
              <MeetingRow key={m.id} meeting={m} active={m.id === selectedId} onClick={() => onSelect(m.id)} />
            ))}
          </>
        )}
      </div>
    </aside>
  );
}

function MeetingRow({ meeting, active, onClick }: { meeting: Meeting; active: boolean; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={clsx(
        'w-full text-left px-4 py-2 border-l-2 transition-colors',
        active
          ? 'border-brand-500 bg-brand-50'
          : 'border-transparent hover:bg-gray-50'
      )}
    >
      <p className="text-sm font-medium text-gray-800 truncate">{meeting.title}</p>
      <p className="text-xs text-gray-400 mt-0.5">
        {meeting.status === 'IN_PROGRESS'
          ? '🔴 In progress'
          : format(new Date(meeting.startedAt), 'MMM d · h:mm a')}
      </p>
    </button>
  );
}

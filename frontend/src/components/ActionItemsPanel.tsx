import { useEffect, useState } from 'react';
import { format } from 'date-fns';
import type { ActionItem, Meeting } from '../types';
import { getActionItems, updateTaskStatus } from '../services/api';
import clsx from 'clsx';

interface Props { meeting: Meeting; }

const STATUS_COLORS = {
  PENDING:     'bg-amber-50 text-amber-700',
  IN_PROGRESS: 'bg-blue-50 text-blue-700',
  DONE:        'bg-green-50 text-green-700',
};

export function ActionItemsPanel({ meeting }: Props) {
  const [items, setItems]     = useState<ActionItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (meeting.status === 'COMPLETED') {
      setLoading(true);
      getActionItems(meeting.id)
        .then(setItems)
        .finally(() => setLoading(false));
    }
  }, [meeting.id, meeting.status]);

  const toggle = async (item: ActionItem) => {
    const next = item.status === 'DONE' ? 'PENDING' : 'DONE';
    const updated = await updateTaskStatus(item.id, next);
    setItems(prev => prev.map(i => i.id === item.id ? updated : i));
  };

  if (meeting.status === 'IN_PROGRESS') {
    return (
      <div className="flex items-center justify-center h-full text-sm text-gray-400">
        Action items will appear after the meeting ends.
      </div>
    );
  }

  if (meeting.status === 'PROCESSING') {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-3 text-sm text-gray-400">
        <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
        Extracting action items…
      </div>
    );
  }

  if (loading) return <div className="p-5 text-sm text-gray-400">Loading action items…</div>;

  if (items.length === 0) {
    return <div className="p-5 text-sm text-gray-400">No action items extracted.</div>;
  }

  return (
    <div className="overflow-y-auto p-5">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-xs text-gray-400 uppercase tracking-wider">
            <th className="text-left pb-3 font-medium w-8"></th>
            <th className="text-left pb-3 font-medium">Task</th>
            <th className="text-left pb-3 font-medium w-28">Assignee</th>
            <th className="text-left pb-3 font-medium w-28">Deadline</th>
            <th className="text-left pb-3 font-medium w-28">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-50">
          {items.map(item => (
            <tr key={item.id} className="group">
              <td className="py-3 pr-2">
                <input
                  type="checkbox"
                  checked={item.status === 'DONE'}
                  onChange={() => toggle(item)}
                  className="w-4 h-4 accent-brand-500 cursor-pointer"
                />
              </td>
              <td className={clsx('py-3 pr-4 text-gray-800 leading-snug', item.status === 'DONE' && 'line-through text-gray-400')}>
                {item.task}
              </td>
              <td className="py-3 pr-4">
                {item.assignee ? (
                  <span className="bg-brand-50 text-brand-500 text-xs font-medium px-2.5 py-1 rounded-full">
                    {item.assignee}
                  </span>
                ) : (
                  <span className="text-gray-300">—</span>
                )}
              </td>
              <td className="py-3 pr-4 text-xs text-gray-500">
                {item.deadline ? format(new Date(item.deadline), 'MMM d, yyyy') : '—'}
              </td>
              <td className="py-3">
                <span className={clsx('text-xs font-medium px-2.5 py-1 rounded-full', STATUS_COLORS[item.status])}>
                  {item.status.replace('_', ' ')}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

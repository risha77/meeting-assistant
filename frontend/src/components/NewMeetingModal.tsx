import { useState } from 'react';
import { X } from 'lucide-react';

interface Props {
  onConfirm: (title: string) => void;
  onClose: () => void;
}

export function NewMeetingModal({ onConfirm, onClose }: Props) {
  const [title, setTitle] = useState('');

  const submit = () => {
    const t = title.trim() || 'Untitled Meeting';
    onConfirm(t);
  };

  return (
    <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl shadow-xl w-96 p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-semibold text-gray-800">New meeting</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X size={18} />
          </button>
        </div>
        <input
          autoFocus
          className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-brand-500"
          placeholder="Meeting title (e.g. Q3 Roadmap Planning)"
          value={title}
          onChange={e => setTitle(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && submit()}
        />
        <div className="flex gap-2 mt-4 justify-end">
          <button onClick={onClose} className="text-sm px-4 py-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50">
            Cancel
          </button>
          <button onClick={submit} className="text-sm px-4 py-2 rounded-lg bg-brand-500 text-white hover:bg-brand-600">
            Create &amp; start
          </button>
        </div>
      </div>
    </div>
  );
}

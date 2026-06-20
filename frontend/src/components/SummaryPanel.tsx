import { useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { FileText, CheckCircle, AlertTriangle, Tag, Download } from 'lucide-react';
import type { Summary, Meeting } from '../types';
import { getSummary } from '../services/api';

interface Props {
  meeting: Meeting;
}

export function SummaryPanel({ meeting }: Props) {
  const [summary, setSummary]   = useState<Summary | null>(null);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  useEffect(() => {
    if (meeting.status === 'COMPLETED') {
      setLoading(true);
      getSummary(meeting.id)
        .then(setSummary)
        .catch(() => setError('Summary not available. The meeting analysis may have failed.'))
        .finally(() => setLoading(false));
    }
  }, [meeting.id, meeting.status]);

  if (meeting.status === 'IN_PROGRESS') {
    return (
      <div className="flex items-center justify-center h-full text-sm text-gray-400">
        Summary will be generated when the meeting ends.
      </div>
    );
  }

  if (meeting.status === 'PROCESSING') {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-3 text-sm text-gray-400">
        <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
        AI analysis in progress — this takes about 30 seconds…
      </div>
    );
  }

  if (loading) return <div className="p-5 text-sm text-gray-400">Generating summary…</div>;

  if (error || !summary) {
    return (
      <div className="p-5">
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 text-sm text-amber-700">
          {error || 'Summary not yet available.'}
        </div>
      </div>
    );
  }

  return (
    <div className="overflow-y-auto p-5 space-y-4">
      {/* Metrics */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          { label: 'Status',   value: meeting.status },
          { label: 'Topics',   value: summary.topics?.split(',').length ?? 0 },
        ].map(m => (
          <div key={m.label} className="bg-gray-50 rounded-xl p-3">
            <p className="text-xs text-gray-400 mb-1">{m.label}</p>
            <p className="text-base font-semibold text-gray-800 truncate">{String(m.value)}</p>
          </div>
        ))}
      </div>

      {/* Summary */}
      <Card icon={<FileText size={14} />} label="Summary">
        <p className="text-sm text-gray-700 leading-relaxed">{summary.summary}</p>
      </Card>

      {/* Decisions */}
      {summary.decisions && (
        <Card icon={<CheckCircle size={14} />} label="Decisions">
          <ul className="space-y-2">
            {summary.decisions.split('\n').filter(Boolean).map((d, i) => (
              <li key={i} className="flex gap-2 text-sm text-gray-700">
                <span className="w-1.5 h-1.5 rounded-full bg-brand-500 mt-2 flex-shrink-0" />
                {d.replace(/^-\s*/, '')}
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* Risks */}
      {summary.risks && (
        <Card icon={<AlertTriangle size={14} />} label="Risks">
          <ul className="space-y-2">
            {summary.risks.split('\n').filter(Boolean).map((r, i) => (
              <li key={i} className="flex gap-2 text-sm text-gray-700">
                <span className="w-1.5 h-1.5 rounded-full bg-amber-400 mt-2 flex-shrink-0" />
                {r.replace(/^-\s*/, '')}
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* Topics */}
      {summary.topics && (
        <Card icon={<Tag size={14} />} label="Topics">
          <div className="flex flex-wrap gap-2">
            {summary.topics.split(',').map(t => (
              <span key={t} className="bg-brand-50 text-brand-500 text-xs font-medium px-2.5 py-1 rounded-full">
                {t.trim()}
              </span>
            ))}
          </div>
        </Card>
      )}

      {/* Export */}
      <button className="flex items-center gap-2 text-sm text-gray-500 border border-gray-200 rounded-lg px-4 py-2 hover:bg-gray-50 transition-colors">
        <Download size={14} />
        Download meeting notes (.txt)
      </button>
    </div>
  );
}

function Card({ icon, label, children }: { icon: ReactNode; label: string; children: ReactNode }) {
  return (
    <div className="bg-white border border-gray-100 rounded-xl p-4">
      <div className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">
        {icon} {label}
      </div>
      {children}
    </div>
  );
}

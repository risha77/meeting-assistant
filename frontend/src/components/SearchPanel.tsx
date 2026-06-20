import { useState, useCallback } from 'react';
import { Search } from 'lucide-react';
import { format } from 'date-fns';
import { searchAll } from '../services/api';
import type { TranscriptSearchResult } from '../types';

export function SearchPanel() {
  const [query, setQuery]     = useState('');
  const [results, setResults] = useState<TranscriptSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const doSearch = useCallback(async (q: string) => {
    setQuery(q);
    if (!q.trim()) { setResults([]); setSearched(false); return; }
    setLoading(true);
    try {
      const res = await searchAll(q.trim());
      setResults(res.results);
      setSearched(true);
    } finally {
      setLoading(false);
    }
  }, []);

  const highlight = (text: string) => {
    if (!query) return text;
    const re = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    const parts = text.split(re);
    return parts.map((p, i) =>
      re.test(p)
        ? <mark key={i} className="bg-brand-50 text-brand-500 rounded px-0.5">{p}</mark>
        : p
    );
  };

  return (
    <div className="flex flex-col h-full">
      <div className="p-4 border-b border-gray-100">
        <div className="flex items-center gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 h-10">
          <Search size={15} className="text-gray-400 flex-shrink-0" />
          <input
            className="flex-1 bg-transparent text-sm outline-none placeholder:text-gray-400"
            placeholder="Search across all meeting transcripts…"
            value={query}
            onChange={e => doSearch(e.target.value)}
          />
        </div>
        <p className="text-xs text-gray-400 mt-2">Powered by PostgreSQL pg_trgm full-text index</p>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {loading && <p className="text-sm text-gray-400">Searching…</p>}

        {!loading && searched && results.length === 0 && (
          <p className="text-sm text-gray-400">No results for <strong>"{query}"</strong></p>
        )}

        {!loading && !searched && (
          <p className="text-sm text-gray-400 text-center mt-12">Type to search transcripts</p>
        )}

        {results.map(r => (
          <div key={r.id} className="bg-white border border-gray-100 rounded-xl p-4 hover:border-gray-200 transition-colors cursor-pointer">
            <p className="text-xs text-gray-400 mb-1">{r.meetingTitle} · {format(new Date(r.ts), 'MMM d, h:mm a')}</p>
            <p className="text-sm text-gray-800 leading-relaxed">{highlight(r.content)}</p>
            <p className="text-xs text-gray-400 mt-2">{r.speaker}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

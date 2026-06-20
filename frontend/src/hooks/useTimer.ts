import { useState, useEffect, useRef } from 'react';

export function useTimer(running: boolean) {
  const [seconds, setSeconds] = useState(0);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (running) {
      setSeconds(0);
      intervalRef.current = setInterval(() => setSeconds(s => s + 1), 1000);
    } else {
      if (intervalRef.current) clearInterval(intervalRef.current);
    }
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, [running]);

  const fmt = (n: number) => String(n).padStart(2, '0');
  const display = `${fmt(Math.floor(seconds / 3600))}:${fmt(Math.floor((seconds % 3600) / 60))}:${fmt(seconds % 60)}`;

  return { seconds, display };
}

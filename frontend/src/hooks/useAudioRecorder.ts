import { useRef, useState, useCallback } from 'react';
import type { WsTranscriptMessage, LiveTurn } from '../types';

interface UseAudioRecorderOptions {
  meetingId: string;
  onTurn: (turn: LiveTurn) => void;
  onError: (msg: string) => void;
}

export function useAudioRecorder({ meetingId, onTurn, onError }: UseAudioRecorderOptions) {
  const [recording, setRecording] = useState(false);

  const wsRef           = useRef<WebSocket | null>(null);
  const mediaStreamRef  = useRef<MediaStream | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const workletNodeRef  = useRef<AudioWorkletNode | null>(null);

  const start = useCallback(async () => {
    if (!meetingId) {
      onError('No meeting selected. Create or select a meeting first.');
      return;
    }

    try {
      // ── 1. Open WebSocket to Spring Boot ──────────────────────────────
      // Use wss:// in production (https), ws:// in development.
      // In dev, Vite proxies /ws/* to backend on :8080.
      // In Docker (nginx), nginx proxies /ws/* to backend too — so no port needed.
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = `${protocol}//${window.location.host}/ws/audio/${meetingId}`;
      const ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      await new Promise<void>((resolve, reject) => {
        ws.onopen = () => resolve();
        ws.onerror = () => reject(new Error('WebSocket failed to connect'));
        setTimeout(() => reject(new Error('WebSocket connection timed out')), 5000);
      });

      // After connection is established, replace handlers for the live session
      ws.onmessage = (evt) => {
        try {
          const msg: WsTranscriptMessage = JSON.parse(evt.data);
          if (msg.type === 'transcript') {
            onTurn({
              speaker:   msg.speaker ?? 'Speaker 1',
              content:   msg.content,
              isFinal:   msg.isFinal ?? false,
              timestamp: msg.timestamp ?? new Date().toISOString(),
            });
          } else if (msg.type === 'error') {
            onError(msg.content);
          }
        } catch (e) {
          console.error('WS parse error', e);
        }
      };

      // Replace connection-phase onerror with runtime error handler
      ws.onerror = () => onError('WebSocket error — is the backend running?');
      ws.onclose = (e) => {
        if (e.code !== 1000) {
          onError(`WebSocket closed unexpectedly (code ${e.code})`);
        }
      };

      // ── 2. Capture microphone at 16 kHz ───────────────────────────────
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          channelCount: 1,
          sampleRate: 16000,
          echoCancellation: true,
          noiseSuppression: true,
        },
      });
      mediaStreamRef.current = stream;

      // ── 3. AudioWorklet (replaces deprecated ScriptProcessorNode) ──────
      const audioContext = new AudioContext({ sampleRate: 16000 });
      audioContextRef.current = audioContext;

      await audioContext.audioWorklet.addModule('/worklets/pcm-processor.js');

      const workletNode = new AudioWorkletNode(audioContext, 'pcm-processor');
      workletNodeRef.current = workletNode;

      // Receive PCM16 frames from worklet and send over WebSocket
      workletNode.port.onmessage = (e: MessageEvent<ArrayBuffer>) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(e.data);
        }
      };

      const source = audioContext.createMediaStreamSource(stream);
      source.connect(workletNode);
      // Don't connect workletNode to destination — we only want to process, not play back

      setRecording(true);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start recording';
      onError(msg);
      cleanup();
    }
  }, [meetingId, onTurn, onError]);

  const stop = useCallback(() => {
    // Signal backend that audio stream is finished
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ type: 'stop' }));
      wsRef.current.close(1000, 'Meeting ended');
    }
    cleanup();
    setRecording(false);
  }, []);

  function cleanup() {
    workletNodeRef.current?.disconnect();
    workletNodeRef.current?.port.close();
    audioContextRef.current?.close();
    mediaStreamRef.current?.getTracks().forEach(t => t.stop());

    wsRef.current         = null;
    workletNodeRef.current  = null;
    audioContextRef.current = null;
    mediaStreamRef.current  = null;
  }

  return { recording, start, stop };
}

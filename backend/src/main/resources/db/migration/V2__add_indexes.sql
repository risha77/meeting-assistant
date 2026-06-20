-- Index on speaker for speaker-based analytics queries
CREATE INDEX IF NOT EXISTS transcript_speaker_idx
    ON transcripts(speaker);

-- Index on meeting status for sidebar filtering
CREATE INDEX IF NOT EXISTS meeting_status_idx
    ON meetings(status);

-- Index on started_at for ordering
CREATE INDEX IF NOT EXISTS meeting_started_at_idx
    ON meetings(started_at DESC);

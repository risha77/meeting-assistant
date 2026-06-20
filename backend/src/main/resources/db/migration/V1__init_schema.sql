-- Enable trigram extension for full-text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Meetings
CREATE TABLE meetings (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title       VARCHAR(255) NOT NULL,
    started_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at    TIMESTAMP,
    status      VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Transcripts
CREATE TABLE transcripts (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id  UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    speaker     VARCHAR(100),
    content     TEXT NOT NULL,
    ts          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- GIN index for fast trigram search
CREATE INDEX transcript_content_idx
    ON transcripts
    USING gin(content gin_trgm_ops);

CREATE INDEX transcript_meeting_idx ON transcripts(meeting_id);

-- Summaries
CREATE TABLE summaries (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id  UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    summary     TEXT,
    decisions   TEXT,
    risks       TEXT,
    topics      TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Action items
CREATE TABLE action_items (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id  UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    assignee    VARCHAR(100),
    task        TEXT NOT NULL,
    deadline    DATE,
    status      VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

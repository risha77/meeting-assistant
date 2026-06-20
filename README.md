# Synrixa Meeting Assistant

A production-grade AI meeting assistant — an Otter.ai clone — built with **Spring Boot**, **React + TypeScript**, **PostgreSQL**, **Deepgram STT**, and **OpenAI GPT-4o**.

---

## Features

| Feature | Technology |
|---|---|
| Real-time audio transcription | Deepgram Nova-2 via WebSocket |
| Speaker diarization | Deepgram diarize=true |
| AI meeting summary | OpenAI GPT-4o |
| Action item extraction | OpenAI GPT-4o (JSON mode) |
| Full-text transcript search | PostgreSQL pg_trgm GIN index |
| Live audio streaming | Browser MediaDevices → Spring Boot WS → Deepgram |
| Persistent storage | PostgreSQL + Flyway migrations |

---

## Project Structure

```
meeting-assistant/
├── backend/                          # Spring Boot 3.2 (Java 21)
│   └── src/main/java/com/synrixa/meetingassistant/
│       ├── controller/
│       │   ├── MeetingController.java      # REST: meetings, summary, tasks
│       │   ├── TranscriptController.java   # REST: transcript turns
│       │   └── SearchController.java       # REST: full-text search
│       ├── service/
│       │   ├── MeetingService.java         # Business logic, async AI analysis
│       │   ├── DeepgramService.java        # Deepgram streaming WS client
│       │   ├── OpenAIService.java          # GPT-4o summary + action items
│       │   ├── TranscriptService.java      # Persist/fetch transcript turns
│       │   └── SearchService.java          # pg_trgm search queries
│       ├── websocket/
│       │   ├── AudioWebSocketHandler.java  # Bridges browser → Deepgram
│       │   └── WebSocketConfig.java
│       ├── entity/                         # JPA entities
│       ├── repository/                     # Spring Data repositories
│       ├── dto/                            # Request/response DTOs
│       └── config/                         # Security, CORS, beans
│
├── frontend/                         # React 18 + TypeScript + Tailwind
│   └── src/
│       ├── pages/MeetingPage.tsx          # Main layout + state
│       ├── components/
│       │   ├── Sidebar.tsx                # Meeting list
│       │   ├── LiveTranscript.tsx         # Real-time transcript view
│       │   ├── SummaryPanel.tsx           # AI summary display
│       │   ├── ActionItemsPanel.tsx       # Action items table
│       │   ├── SearchPanel.tsx            # Full-text search UI
│       │   └── NewMeetingModal.tsx        # Create meeting dialog
│       ├── hooks/
│       │   ├── useAudioRecorder.ts        # Mic capture + WS streaming
│       │   └── useTimer.ts               # Meeting timer
│       ├── services/api.ts               # Axios API calls
│       └── types/index.ts               # Shared TypeScript types
│
└── docker-compose.yml                # PostgreSQL + backend + frontend
```

---

## Quick Start

### 1. Prerequisites

- Java 21+
- Node.js 20+
- Docker + Docker Compose (for PostgreSQL)
- Deepgram API key → https://console.deepgram.com
- OpenAI API key → https://platform.openai.com

### 2. Environment setup

```bash
cp .env.example .env
# Edit .env and add your API keys:
# DEEPGRAM_API_KEY=...
# OPENAI_API_KEY=...
```

### 3. Start PostgreSQL

```bash
docker-compose up postgres -d
```

### 4. Run the backend

```bash
cd backend
./mvnw spring-boot:run
# Server starts on http://localhost:8080
# Flyway runs migrations automatically
```

### 5. Run the frontend

```bash
cd frontend
npm install
npm run dev
# App starts on http://localhost:3000
```

### 6. Full stack with Docker

```bash
docker-compose up --build
# Frontend: http://localhost:3000
# Backend:  http://localhost:8080
```

---

## REST API Reference

### Meetings

```
POST   /api/meetings                    Create a new meeting
GET    /api/meetings                    List all meetings
GET    /api/meetings/{id}              Get meeting details
POST   /api/meetings/{id}/end          End meeting & trigger AI analysis
GET    /api/meetings/{id}/summary      Get AI-generated summary
GET    /api/meetings/{id}/tasks        Get extracted action items
PATCH  /api/meetings/tasks/{taskId}    Update action item status
```

### Transcripts

```
GET    /api/meetings/{id}/transcripts  Get all transcript turns
```

### Search

```
GET    /api/search?q=query             Full-text search across all meetings
GET    /api/search/meetings/{id}?q=   Search within a specific meeting
```

### WebSocket

```
WS     /ws/audio/{meetingId}          Stream raw PCM16 audio bytes
```

---

## Audio Pipeline

```
Browser Mic (getUserMedia)
    ↓ Float32 PCM
AudioContext (16 kHz resample)
    ↓ PCM16 binary frames
Spring Boot WebSocket (/ws/audio/{meetingId})
    ↓ forward bytes
Deepgram Streaming API (wss://api.deepgram.com)
    ↓ JSON transcript events
AudioWebSocketHandler (parse + push to frontend)
    ↓ final turns only
TranscriptService → PostgreSQL
```

---

## AI Analysis Pipeline (async, post-meeting)

```
endMeeting() called
    ↓
MeetingService.analyseAsync() [@Async]
    ↓
TranscriptRepository.getFullTranscript()     ← concatenated turns
    ↓                       ↓
OpenAIService               OpenAIService
.generateSummary()          .extractActionItems()
    ↓                           ↓
SummaryRepository         ActionItemRepository
.save()                   .saveAll()
    ↓
Meeting.status = COMPLETED
```

---

## Database Schema

```sql
meetings     (id, title, started_at, ended_at, status, created_at)
transcripts  (id, meeting_id, speaker, content, ts)
summaries    (id, meeting_id, summary, decisions, risks, topics, created_at)
action_items (id, meeting_id, assignee, task, deadline, status, created_at)

-- Full-text search index
CREATE INDEX transcript_content_idx
  ON transcripts USING gin(content gin_trgm_ops);
```

---

## Configuration

All configuration is in `backend/src/main/resources/application.yml`.
Environment variables override defaults:

| Variable | Description |
|---|---|
| `DEEPGRAM_API_KEY` | Your Deepgram API key (required) |
| `OPENAI_API_KEY` | Your OpenAI API key (required) |
| `DB_USERNAME` | PostgreSQL username (default: postgres) |
| `DB_PASSWORD` | PostgreSQL password (default: postgres) |

---

## Tech Stack

**Backend**
- Spring Boot 3.2, Java 21
- Spring WebSocket (binary handler)
- Spring Data JPA + Hibernate
- Flyway (schema migrations)
- OkHttp (Deepgram WS client)
- OpenAI REST API
- PostgreSQL 16 + pg_trgm

**Frontend**
- React 18 + TypeScript
- Vite
- Tailwind CSS
- Axios
- date-fns
- lucide-react

**Infrastructure**
- Docker + Docker Compose
- Nginx (frontend production server + reverse proxy)

package com.synrixa.meetingassistant.dto;

import com.synrixa.meetingassistant.entity.ActionItem;
import com.synrixa.meetingassistant.entity.Meeting;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Dtos {

    // ── Meeting ──────────────────────────────────────────────
    @Data
    public static class MeetingRequest {
        private String title;
    }

    @Data @Builder
    public static class MeetingResponse {
        private UUID id;
        private String title;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Meeting.MeetingStatus status;

        public static MeetingResponse from(Meeting m) {
            return MeetingResponse.builder()
                    .id(m.getId())
                    .title(m.getTitle())
                    .startedAt(m.getStartedAt())
                    .endedAt(m.getEndedAt())
                    .status(m.getStatus())
                    .build();
        }
    }

    // ── Transcript ───────────────────────────────────────────
    @Data @Builder
    public static class TranscriptResponse {
        private UUID id;
        private UUID meetingId;
        private String speaker;
        private String content;
        private LocalDateTime ts;
    }

    @Data @Builder
    public static class TranscriptSearchResult {
        private UUID id;
        private UUID meetingId;
        private String meetingTitle;
        private String speaker;
        private String content;
        private LocalDateTime ts;
    }

    // ── Summary ──────────────────────────────────────────────
    @Data @Builder
    public static class SummaryResponse {
        private UUID id;
        private UUID meetingId;
        private String summary;
        private String decisions;
        private String risks;
        private String topics;
        private LocalDateTime createdAt;
    }

    // ── Action Items ─────────────────────────────────────────
    @Data @Builder
    public static class ActionItemResponse {
        private UUID id;
        private UUID meetingId;
        private String assignee;
        private String task;
        private LocalDate deadline;
        private ActionItem.ActionStatus status;

        public static ActionItemResponse from(ActionItem a) {
            return ActionItemResponse.builder()
                    .id(a.getId())
                    .meetingId(a.getMeeting().getId())
                    .assignee(a.getAssignee())
                    .task(a.getTask())
                    .deadline(a.getDeadline())
                    .status(a.getStatus())
                    .build();
        }
    }

    @Data
    public static class ActionItemUpdateRequest {
        private ActionItem.ActionStatus status;
    }

    // ── OpenAI Parsed ────────────────────────────────────────
    @Data
    public static class ParsedActionItem {
        private String assignee;
        private String task;
        private String deadline;
    }

    // ── WebSocket message ────────────────────────────────────
    @Data @Builder
    public static class TranscriptMessage {
        private String type;   // "transcript" | "error" | "connected"
        private String speaker;
        private String content;
        private boolean isFinal;
        private String timestamp;
    }

    // ── Search ───────────────────────────────────────────────
    @Data @Builder
    public static class SearchResponse {
        private String query;
        private int total;
        private List<TranscriptSearchResult> results;
    }
}
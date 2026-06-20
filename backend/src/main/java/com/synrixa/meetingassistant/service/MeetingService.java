package com.synrixa.meetingassistant.service;

import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.entity.*;
import com.synrixa.meetingassistant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final SummaryRepository summaryRepository;
    private final ActionItemRepository actionItemRepository;
    private final TranscriptService transcriptService;
    private final OpenAIService openAIService;

    /**
     * Self-reference via @Lazy so that the Spring proxy wraps the @Async call.
     * Without this, calling analyseAsync() from within the same bean bypasses
     * the proxy and @Async has no effect (the analysis runs on the request thread).
     */
    @Autowired
    @Lazy
    private MeetingService self;

    @Transactional
    public Dtos.MeetingResponse createMeeting(String title) {
        Meeting meeting = Meeting.builder()
                .title(title)
                .status(Meeting.MeetingStatus.IN_PROGRESS)
                .build();
        return Dtos.MeetingResponse.from(meetingRepository.save(meeting));
    }

    @Transactional
    public Dtos.MeetingResponse endMeeting(UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));

        meeting.setEndedAt(LocalDateTime.now());
        meeting.setStatus(Meeting.MeetingStatus.PROCESSING);
        Meeting saved = meetingRepository.save(meeting);

        // Call through self proxy so @Async is honoured
        self.analyseAsync(saved.getId());

        return Dtos.MeetingResponse.from(saved);
    }

    /**
     * Runs in the "analysisExecutor" thread pool.
     * Fetches transcript, calls OpenAI for summary + action items, persists results.
     */
    @Async("analysisExecutor")
    @Transactional
    public void analyseAsync(UUID meetingId) {
        // Re-load inside new thread — the Meeting passed from endMeeting() is detached
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalStateException("Meeting disappeared: " + meetingId));
        try {
            String fullTranscript = transcriptService.getFullTranscript(meetingId);
            if (fullTranscript == null || fullTranscript.isBlank()) {
                log.warn("No transcript found for meeting {}", meetingId);
                meeting.setStatus(Meeting.MeetingStatus.COMPLETED);
                meetingRepository.save(meeting);
                return;
            }

            // Generate summary
            Dtos.SummaryResponse summaryDto = openAIService.generateSummary(fullTranscript);
            Summary summary = Summary.builder()
                    .meeting(meeting)
                    .summary(summaryDto.getSummary())
                    .decisions(summaryDto.getDecisions())
                    .risks(summaryDto.getRisks())
                    .topics(summaryDto.getTopics())
                    .build();
            summaryRepository.save(summary);

            // Extract action items
            List<Dtos.ParsedActionItem> parsedItems = openAIService.extractActionItems(fullTranscript);
            for (Dtos.ParsedActionItem item : parsedItems) {
                ActionItem actionItem = ActionItem.builder()
                        .meeting(meeting)
                        .assignee(item.getAssignee())
                        .task(item.getTask())
                        .deadline(parseDeadline(item.getDeadline()))
                        .status(ActionItem.ActionStatus.PENDING)
                        .build();
                actionItemRepository.save(actionItem);
            }

            meeting.setStatus(Meeting.MeetingStatus.COMPLETED);
            meetingRepository.save(meeting);
            log.info("AI analysis completed for meeting {}", meetingId);

        } catch (Exception e) {
            log.error("AI analysis failed for meeting {}: {}", meetingId, e.getMessage(), e);
            meeting.setStatus(Meeting.MeetingStatus.COMPLETED);
            meetingRepository.save(meeting);
        }
    }

    @Transactional(readOnly = true)
    public List<Dtos.MeetingResponse> getAllMeetings() {
        return meetingRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(Dtos.MeetingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Dtos.MeetingResponse getMeeting(UUID meetingId) {
        return meetingRepository.findById(meetingId)
                .map(Dtos.MeetingResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));
    }

    @Transactional(readOnly = true)
    public Dtos.SummaryResponse getSummary(UUID meetingId) {
        return summaryRepository.findByMeetingId(meetingId)
                .map(s -> Dtos.SummaryResponse.builder()
                        .id(s.getId())
                        .meetingId(meetingId)
                        .summary(s.getSummary())
                        .decisions(s.getDecisions())
                        .risks(s.getRisks())
                        .topics(s.getTopics())
                        .createdAt(s.getCreatedAt())
                        .build())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Summary not yet available for meeting: " + meetingId));
    }

    @Transactional(readOnly = true)
    public List<Dtos.ActionItemResponse> getActionItems(UUID meetingId) {
        return actionItemRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId)
                .stream()
                .map(Dtos.ActionItemResponse::from)
                .toList();
    }

    @Transactional
    public Dtos.ActionItemResponse updateActionItemStatus(UUID itemId, ActionItem.ActionStatus status) {
        ActionItem item = actionItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Action item not found: " + itemId));
        item.setStatus(status);
        return Dtos.ActionItemResponse.from(actionItemRepository.save(item));
    }

    private LocalDate parseDeadline(String deadline) {
        if (deadline == null || deadline.isBlank()) return null;
        try {
            return LocalDate.parse(deadline);
        } catch (Exception e) {
            return null;
        }
    }
}

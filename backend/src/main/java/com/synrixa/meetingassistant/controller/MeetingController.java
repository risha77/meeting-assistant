package com.synrixa.meetingassistant.controller;

import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /** POST /api/meetings — Create and start a new meeting */
    @PostMapping
    public ResponseEntity<Dtos.MeetingResponse> createMeeting(
            @RequestBody Dtos.MeetingRequest request) {
        return ResponseEntity.ok(meetingService.createMeeting(request.getTitle()));
    }

    /** GET /api/meetings — List all meetings */
    @GetMapping
    public ResponseEntity<List<Dtos.MeetingResponse>> getAllMeetings() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    /** GET /api/meetings/{id} — Get a single meeting */
    @GetMapping("/{id}")
    public ResponseEntity<Dtos.MeetingResponse> getMeeting(@PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.getMeeting(id));
    }

    /** POST /api/meetings/{id}/end — End a meeting and trigger AI analysis */
    @PostMapping("/{id}/end")
    public ResponseEntity<Dtos.MeetingResponse> endMeeting(@PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.endMeeting(id));
    }

    /** GET /api/meetings/{id}/summary — Get AI-generated summary */
    @GetMapping("/{id}/summary")
    public ResponseEntity<Dtos.SummaryResponse> getSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.getSummary(id));
    }

    /** GET /api/meetings/{id}/tasks — Get extracted action items */
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<Dtos.ActionItemResponse>> getActionItems(@PathVariable UUID id) {
        return ResponseEntity.ok(meetingService.getActionItems(id));
    }

    /** PATCH /api/meetings/tasks/{taskId} — Update action item status */
    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<Dtos.ActionItemResponse> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestBody Dtos.ActionItemUpdateRequest request) {
        return ResponseEntity.ok(meetingService.updateActionItemStatus(taskId, request.getStatus()));
    }
}

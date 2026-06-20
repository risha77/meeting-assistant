package com.synrixa.meetingassistant.controller;

import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings/{meetingId}/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    /** GET /api/meetings/{meetingId}/transcripts — Get all transcript turns for a meeting */
    @GetMapping
    public ResponseEntity<List<Dtos.TranscriptResponse>> getTranscripts(
            @PathVariable UUID meetingId) {
        return ResponseEntity.ok(transcriptService.getForMeeting(meetingId));
    }
}

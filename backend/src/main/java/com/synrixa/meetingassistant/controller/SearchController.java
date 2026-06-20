package com.synrixa.meetingassistant.controller;

import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /** GET /api/search?q=query — Full-text search across all meetings */
    @GetMapping
    public ResponseEntity<Dtos.SearchResponse> search(
            @RequestParam("q") String query) {
        return ResponseEntity.ok(searchService.search(query));
    }

    /** GET /api/search/meetings/{meetingId}?q=query — Search within a specific meeting */
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<Dtos.SearchResponse> searchInMeeting(
            @PathVariable UUID meetingId,
            @RequestParam("q") String query) {
        return ResponseEntity.ok(searchService.searchInMeeting(meetingId, query));
    }
}

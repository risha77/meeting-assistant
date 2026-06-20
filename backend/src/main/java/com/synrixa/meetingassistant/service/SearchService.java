package com.synrixa.meetingassistant.service;

import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.entity.Transcript;
import com.synrixa.meetingassistant.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TranscriptRepository transcriptRepository;

    @Transactional(readOnly = true)
    public Dtos.SearchResponse search(String query) {
        List<Transcript> results = transcriptRepository.searchByContent(
                query, PageRequest.of(0, 50));

        List<Dtos.TranscriptSearchResult> mapped = results.stream()
                .map(t -> Dtos.TranscriptSearchResult.builder()
                        .id(t.getId())
                        .meetingId(t.getMeeting() != null ? t.getMeeting().getId() : null)
                        .meetingTitle(t.getMeeting() != null ? t.getMeeting().getTitle() : "Unknown")
                        .speaker(t.getSpeaker())
                        .content(t.getContent())
                        .ts(t.getTs())
                        .build())
                .toList();

        return Dtos.SearchResponse.builder()
                .query(query)
                .total(mapped.size())
                .results(mapped)
                .build();
    }

    @Transactional(readOnly = true)
    public Dtos.SearchResponse searchInMeeting(UUID meetingId, String query) {
        List<Transcript> results = transcriptRepository.searchInMeeting(meetingId, query);

        List<Dtos.TranscriptSearchResult> mapped = results.stream()
                .map(t -> Dtos.TranscriptSearchResult.builder()
                        .id(t.getId())
                        .meetingId(meetingId)
                        .meetingTitle(t.getMeeting() != null ? t.getMeeting().getTitle() : "")
                        .speaker(t.getSpeaker())
                        .content(t.getContent())
                        .ts(t.getTs())
                        .build())
                .toList();

        return Dtos.SearchResponse.builder()
                .query(query)
                .total(mapped.size())
                .results(mapped)
                .build();
    }
}

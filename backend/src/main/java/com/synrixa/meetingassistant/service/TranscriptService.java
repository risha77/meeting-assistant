package com.synrixa.meetingassistant.service;

import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.entity.Meeting;
import com.synrixa.meetingassistant.entity.Transcript;
import com.synrixa.meetingassistant.repository.MeetingRepository;
import com.synrixa.meetingassistant.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptService {

    private final TranscriptRepository transcriptRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public Transcript save(UUID meetingId, String speaker, String content) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));

        Transcript transcript = Transcript.builder()
                .meeting(meeting)
                .speaker(speaker)
                .content(content)
                .build();

        return transcriptRepository.save(transcript);
    }

    @Transactional(readOnly = true)
    public List<Dtos.TranscriptResponse> getForMeeting(UUID meetingId) {
        return transcriptRepository.findByMeetingIdOrderByTsAsc(meetingId)
                .stream()
                .map(t -> Dtos.TranscriptResponse.builder()
                        .id(t.getId())
                        .meetingId(t.getMeeting().getId())
                        .speaker(t.getSpeaker())
                        .content(t.getContent())
                        .ts(t.getTs())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public String getFullTranscript(UUID meetingId) {
        return transcriptRepository.getFullTranscript(meetingId);
    }
}

//package com.synrixa.meetingassistant;
//
//import com.synrixa.meetingassistant.dto.Dtos;
//import com.synrixa.meetingassistant.entity.Meeting;
//import com.synrixa.meetingassistant.entity.Meeting.MeetingStatus;
//import com.synrixa.meetingassistant.repository.ActionItemRepository;
//import com.synrixa.meetingassistant.repository.MeetingRepository;
//import com.synrixa.meetingassistant.repository.SummaryRepository;
//import com.synrixa.meetingassistant.service.MeetingService;
//import com.synrixa.meetingassistant.service.OpenAIService;
//import com.synrixa.meetingassistant.service.TranscriptService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MeetingServiceTest {
//
//    @Mock MeetingRepository    meetingRepository;
//    @Mock SummaryRepository    summaryRepository;
//    @Mock ActionItemRepository actionItemRepository;
//    @Mock TranscriptService    transcriptService;
//    @Mock OpenAIService        openAIService;
//
//    @InjectMocks
//    MeetingService meetingService;
//
//    @BeforeEach
//    void injectSelf() throws Exception {
//        // Wire the self-reference that @Lazy normally provides via Spring proxy.
//        var field = MeetingService.class.getDeclaredField("self");
//        field.setAccessible(true);
//        field.set(meetingService, meetingService);
//    }
//
//    // ── createMeeting ────────────────────────────────────────────────────
//
//    @Test
//    void createMeeting_returnsResponseWithCorrectTitle() {
//        Meeting saved = Meeting.builder()
//                .id(UUID.randomUUID())
//                .title("Test Meeting")
//                .status(MeetingStatus.IN_PROGRESS)
//                .build();
//        when(meetingRepository.save(any())).thenReturn(saved);
//
//        Dtos.MeetingResponse result = meetingService.createMeeting("Test Meeting");
//
//        assertThat(result.getTitle()).isEqualTo("Test Meeting");
//        assertThat(result.getStatus()).isEqualTo(MeetingStatus.IN_PROGRESS);
//    }
//
//    @Test
//    void createMeeting_persistsNewEntity() {
//        when(meetingRepository.save(any())).thenAnswer(inv -> {
//            Meeting m = inv.getArgument(0);
//            m = Meeting.builder()
//                    .id(UUID.randomUUID())
//                    .title(m.getTitle())
//                    .status(m.getStatus())
//                    .build();
//            return m;
//        });
//
//        meetingService.createMeeting("Sprint Review");
//
//        verify(meetingRepository, times(1)).save(any(Meeting.class));
//    }
//
//    // ── endMeeting ───────────────────────────────────────────────────────
//
//    @Test
//    void endMeeting_setsStatusProcessingAndEndedAt() {
//        UUID id = UUID.randomUUID();
//        Meeting existing = Meeting.builder()
//                .id(id).title("Test").status(MeetingStatus.IN_PROGRESS).build();
//
//        when(meetingRepository.findById(id)).thenReturn(Optional.of(existing));
//        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//        // analyseAsync will call findById again on a new thread — return same meeting
//        when(transcriptService.getFullTranscript(id)).thenReturn(null);
//
//        Dtos.MeetingResponse result = meetingService.endMeeting(id);
//
//        assertThat(result.getStatus()).isEqualTo(MeetingStatus.PROCESSING);
//        assertThat(result.getEndedAt()).isNotNull();
//    }
//
//    @Test
//    void endMeeting_throwsWhenMeetingNotFound() {
//        UUID id = UUID.randomUUID();
//        when(meetingRepository.findById(id)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> meetingService.endMeeting(id))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("Meeting not found");
//    }
//
//    // ── getSummary ───────────────────────────────────────────────────────
//
//    @Test
//    void getSummary_throwsWhenNotFound() {
//        UUID id = UUID.randomUUID();
//        when(summaryRepository.findByMeetingId(id)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> meetingService.getSummary(id))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("Summary not yet available");
//    }
//
//    // ── getMeeting ───────────────────────────────────────────────────────
//
//    @Test
//    void getMeeting_throwsWhenNotFound() {
//        UUID id = UUID.randomUUID();
//        when(meetingRepository.findById(id)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> meetingService.getMeeting(id))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("Meeting not found");
//    }
//}

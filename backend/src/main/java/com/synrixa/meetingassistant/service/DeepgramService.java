package com.synrixa.meetingassistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synrixa.meetingassistant.config.AppProperties;
import com.synrixa.meetingassistant.dto.Dtos;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@Slf4j
public class DeepgramService {

    private final AppProperties props;
    private final ObjectMapper mapper;
    private final TranscriptService transcriptService;
    private final OkHttpClient httpClient;

    // meetingId -> active Deepgram WebSocket
    private final ConcurrentHashMap<UUID, WebSocket> deepgramSessions = new ConcurrentHashMap<>();

    public DeepgramService(AppProperties props,
                           ObjectMapper mapper,
                           TranscriptService transcriptService) {
        this.props = props;
        this.mapper = mapper;
        this.transcriptService = transcriptService;
        this.httpClient = new OkHttpClient.Builder().build();
    }

    /**
     * Open a Deepgram streaming session for the given meeting.
     * All messages (transcripts, errors, connection status) are delivered
     * exclusively via the onTranscript callback — the caller decides how to forward them.
     */
    public void openSession(UUID meetingId,
                            Consumer<Dtos.TranscriptMessage> onTranscript) {

        AppProperties.Deepgram dg = props.getDeepgram();

        String url = dg.getUrl()
                + "?model=" + dg.getModel()
                + "&language=" + dg.getLanguage()
                + "&punctuate=" + dg.isPunctuate()
                + "&diarize=" + dg.isDiarize()
                + "&smart_format=" + dg.isSmartFormat()
                + "&encoding=linear16&sample_rate=16000";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token " + dg.getApiKey())
                .build();

        WebSocket ws = httpClient.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.info("Deepgram session opened for meeting {}", meetingId);
                onTranscript.accept(Dtos.TranscriptMessage.builder()
                        .type("connected")
                        .content("Deepgram streaming connected")
                        .build());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode root = mapper.readTree(text);
                    JsonNode channel = root.path("channel");
                    JsonNode alternatives = channel.path("alternatives");

                    if (alternatives.isEmpty()) return;

                    String transcript = alternatives.get(0).path("transcript").asText();
                    if (transcript.isBlank()) return;

                    boolean isFinal = root.path("is_final").asBoolean(false);
                    String speaker = extractSpeaker(alternatives.get(0));

                    Dtos.TranscriptMessage msg = Dtos.TranscriptMessage.builder()
                            .type("transcript")
                            .speaker(speaker)
                            .content(transcript)
                            .isFinal(isFinal)
                            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .build();

                    onTranscript.accept(msg);

                    // Persist only final transcripts
                    if (isFinal) {
                        transcriptService.save(meetingId, speaker, transcript);
                    }

                } catch (Exception e) {
                    log.error("Error parsing Deepgram response for meeting {}: {}", meetingId, e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("Deepgram WebSocket failure for meeting {}: {}", meetingId, t.getMessage());
                deepgramSessions.remove(meetingId);
                onTranscript.accept(Dtos.TranscriptMessage.builder()
                        .type("error")
                        .content("Deepgram connection lost: " + t.getMessage())
                        .build());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                log.info("Deepgram session closing for meeting {}: {} {}", meetingId, code, reason);
                deepgramSessions.remove(meetingId);
            }
        });

        deepgramSessions.put(meetingId, ws);
    }

    /**
     * Forward raw audio bytes from the browser to Deepgram.
     */
    public void sendAudio(UUID meetingId, byte[] audio) {
        WebSocket ws = deepgramSessions.get(meetingId);
        if (ws != null) {
            ws.send(ByteString.of(audio));
        } else {
            log.warn("No Deepgram session found for meeting {}", meetingId);
        }
    }

    /**
     * Close the Deepgram session for a meeting.
     */
    public void closeSession(UUID meetingId) {
        WebSocket ws = deepgramSessions.remove(meetingId);
        if (ws != null) {
            ws.close(1000, "Meeting ended");
            log.info("Closed Deepgram session for meeting {}", meetingId);
        }
    }

    private String extractSpeaker(JsonNode alternative) {
        JsonNode words = alternative.path("words");
        if (!words.isEmpty()) {
            int speakerNum = words.get(0).path("speaker").asInt(0);
            return "Speaker " + (speakerNum + 1);
        }
        return "Speaker 1";
    }
}

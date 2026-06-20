package com.synrixa.meetingassistant.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synrixa.meetingassistant.dto.Dtos;
import com.synrixa.meetingassistant.service.DeepgramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AudioWebSocketHandler extends AbstractWebSocketHandler {

    private final DeepgramService deepgramService;
    private final ObjectMapper mapper;

    // sessionId -> meetingId
    private final Map<String, UUID> sessionMeetingMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath();  // /ws/audio/{meetingId}
        String[] parts = path.split("/");
        UUID meetingId = UUID.fromString(parts[parts.length - 1]);
        sessionMeetingMap.put(session.getId(), meetingId);

        log.info("Frontend WebSocket connected: session={} meeting={}", session.getId(), meetingId);

        // Open Deepgram session; push all messages back to this frontend session via callback
        deepgramService.openSession(meetingId, transcript -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(transcript)));
                }
            } catch (IOException e) {
                log.error("Failed to forward transcript to frontend session {}: {}",
                        session.getId(), e.getMessage());
            }
        });
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        UUID meetingId = sessionMeetingMap.get(session.getId());
        if (meetingId == null) {
            log.warn("Binary message received but no meeting mapping for session {}", session.getId());
            return;
        }
        byte[] audio = message.getPayload().array();
        deepgramService.sendAudio(meetingId, audio);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle control messages from frontend (e.g., { "type": "stop" })
        try {
            Map<String, String> msg = mapper.readValue(
                    message.getPayload(), new TypeReference<Map<String, String>>() {});
            if ("stop".equals(msg.get("type"))) {
                UUID meetingId = sessionMeetingMap.get(session.getId());
                if (meetingId != null) {
                    deepgramService.closeSession(meetingId);
                }
            }
        } catch (Exception e) {
            log.debug("Non-JSON text message received, ignoring: {}", message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID meetingId = sessionMeetingMap.remove(session.getId());
        if (meetingId != null) {
            deepgramService.closeSession(meetingId);
            log.info("Frontend WebSocket closed: session={} meeting={} status={}", session.getId(), meetingId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }
}

package com.synrixa.meetingassistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synrixa.meetingassistant.config.AppProperties;
import com.synrixa.meetingassistant.dto.Dtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final AppProperties props;
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    private static final String OPENAI_URL = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";
    /**
     * Generate a structured meeting summary from the full transcript.
     */
    public Dtos.SummaryResponse generateSummary(String fullTranscript) {
        String prompt = """
                You are an expert meeting analyst. Analyse the following meeting transcript and return a JSON object with exactly these keys:
                - "summary": A concise paragraph summarising the meeting (2-4 sentences).
                - "decisions": A newline-separated list of key decisions made (prefix each with "- ").
                - "risks": A newline-separated list of risks or concerns raised (prefix each with "- ").
                - "topics": A comma-separated list of main topics discussed.

                Respond with valid JSON only — no markdown fences, no extra text.

                Transcript:
                %s
                """.formatted(fullTranscript);

        String raw = callOpenAI(prompt);

        try {
            // Strip accidental markdown fences if the model disobeys
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            Map<String, String> parsed = mapper.readValue(cleaned, new TypeReference<>() {});
            return Dtos.SummaryResponse.builder()
                    .summary(parsed.getOrDefault("summary", ""))
                    .decisions(parsed.getOrDefault("decisions", ""))
                    .risks(parsed.getOrDefault("risks", ""))
                    .topics(parsed.getOrDefault("topics", ""))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse OpenAI summary response: {}", e.getMessage());
            // Fallback: return raw text as summary
            return Dtos.SummaryResponse.builder().summary(raw).build();
        }
    }

    /**
     * Extract structured action items from the transcript.
     */
    public List<Dtos.ParsedActionItem> extractActionItems(String fullTranscript) {
        String prompt = """
                You are an expert meeting analyst. Extract all action items from the following meeting transcript.
                Return a JSON array where each element has:
                - "assignee": the person responsible (string, or "" if unknown)
                - "task": clear description of what needs to be done (string)
                - "deadline": ISO date string YYYY-MM-DD if mentioned, or "" if not specified

                Respond with a valid JSON array only — no markdown fences, no extra text.

                Transcript:
                %s
                """.formatted(fullTranscript);

        String raw = callOpenAI(prompt);

        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            return mapper.readValue(cleaned, new TypeReference<List<Dtos.ParsedActionItem>>() {});
        } catch (Exception e) {
            log.error("Failed to parse OpenAI action items response: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Call OpenAI chat completions and return the assistant message content string.
     * Uses Jackson JsonNode for type-safe parsing instead of raw Map casts.
     */
    private String callOpenAI(String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(props.getOpenai().getApiKey());

        Map<String, Object> body = Map.of(
                "model", props.getOpenai().getModel(),
                "max_tokens", props.getOpenai().getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENAI_URL, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("OpenAI returned status: " + response.getStatusCode());
            }

            // Type-safe parse via JsonNode — no raw Map casts
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content");

            if (content.isMissingNode() || content.isNull()) {
                throw new RuntimeException("OpenAI response missing content field");
            }

            return content.asText();

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }
}

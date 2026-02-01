package com.gallary.gallaryV1.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.gallary.gallaryV1.dto.GeminiResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

        @Value("${gemini.api.key}")
        private String apiKey;

        @Value("${gemini.model.name}")
        private String modelName;

        @Value("${gemini.prompt.template}")
        private String promptTemplate;

        // Use RestClient for the most modern Spring 3.x approach
        private final RestClient restClient = RestClient.create();
        private final ObjectMapper objectMapper = new ObjectMapper();

        public GeminiResult analyzeImage(byte[] imageBytes) {
                try {
                        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                        // 2. The URL (using v1beta as it supports the 2.5 series features)
                        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                                        + modelName + ":generateContent?key=" + apiKey;

                        // 3. Request Payload
                        Map<String, Object> body = Map.of(
                                        "contents", List.of(
                                                        Map.of("parts", List.of(
                                                                        Map.of("text", promptTemplate),
                                                                        Map.of("inline_data", Map.of(
                                                                                        "mime_type", "image/jpeg",
                                                                                        "data", base64Image))))));

                        // 4. Send the request
                        String response = restClient.post()
                                        .uri(url)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(body)
                                        .retrieve()
                                        .body(String.class);

                        return parseGeminiResponse(response);

                } catch (Exception e) {
                        // If you hit a 429 error here, it means you've used your daily 2.5-flash quota.
                        throw new RuntimeException("AI Analysis failed: " + e.getMessage());
                }
        }

        private GeminiResult parseGeminiResponse(String json) throws Exception {
                JsonNode root = objectMapper.readTree(json);

                // --- Robust parsing start ---
                if (!root.has("candidates") || !root.path("candidates").isArray()
                                || root.path("candidates").isEmpty()) {
                        throw new RuntimeException(
                                        "AI response is missing 'candidates' field or it's empty. Full response: "
                                                        + json);
                }

                JsonNode firstCandidate = root.path("candidates").get(0);

                if (!firstCandidate.has("content") || !firstCandidate.path("content").has("parts") ||
                                !firstCandidate.path("content").path("parts").isArray() ||
                                firstCandidate.path("content").path("parts").isEmpty()) {
                        throw new RuntimeException(
                                        "AI response is missing 'parts' field or it's empty. Full response: " + json);
                }

                String text = firstCandidate.path("content").path("parts").get(0).path("text").asText();
                // --- Robust parsing end ---

                // Clean any Markdown formatting (```json ... ```)
                String cleanJson = text.replaceAll("(?s)```json\\s*|\\s*```", "").trim();
                JsonNode result = objectMapper.readTree(cleanJson);

                List<String> rawTags = objectMapper.convertValue(result.get("tags"), List.class);
                List<String> uniqueNormalizedTags = rawTags.stream()
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .distinct()
                                .toList();

                return new GeminiResult(
                                result.get("description").asText(),
                                uniqueNormalizedTags);
        }
}
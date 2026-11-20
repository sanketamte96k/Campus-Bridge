package com.campusbridge.backend.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GeminiLlmClient implements LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiLlmClient.class);

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutMs;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public GeminiLlmClient(
            @Value("${llm.api-key:}") String apiKey,
            @Value("${llm.model:gemini-2.5-flash}") String model,
            @Value("${llm.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
            @Value("${llm.timeout-ms:15000}") int timeoutMs,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null && !model.isBlank() ? model.trim() : "gemini-2.5-flash";
        this.baseUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl.trim().replaceAll("/+$", "") : "https://generativelanguage.googleapis.com";
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 15000;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(this.timeoutMs);
        requestFactory.setReadTimeout(this.timeoutMs);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    // Constructor for testing with custom RestClient
    public GeminiLlmClient(String apiKey, String model, String baseUrl, int timeoutMs, RestClient restClient, ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null ? model.trim() : "gemini-2.5-flash";
        this.baseUrl = baseUrl != null ? baseUrl.trim().replaceAll("/+$", "") : "https://generativelanguage.googleapis.com";
        this.timeoutMs = timeoutMs;
        this.restClient = restClient;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public String generate(String systemInstruction, String userJson) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("LLM_API_KEY is not configured");
        }

        try {
            return executeWithRetry(systemInstruction, userJson);
        } catch (Exception e) {
            logger.warn("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("LLM generation failed: " + e.getMessage(), e);
        }
    }

    private String executeWithRetry(String systemInstruction, String userJson) {
        try {
            return callGemini(systemInstruction, userJson);
        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            if (statusCode == 429 || (statusCode >= 500 && statusCode < 600)) {
                logger.warn("Gemini API responded with status {}. Retrying once with backoff...", statusCode);
                try {
                    Thread.sleep(1000); // 1s short backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
                return callGemini(systemInstruction, userJson);
            }
            throw ex;
        }
    }

    private String callGemini(String systemInstruction, String userJson) {
        String endpointUrl = baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", userJson))
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        String responseJson = restClient.post()
                .uri(endpointUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return extractCandidateText(responseJson);
    }

    private String extractCandidateText(String responseJson) {
        if (responseJson == null || responseJson.isBlank()) {
            throw new IllegalStateException("Empty response from Gemini API");
        }

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException("No candidate returned by Gemini API");
            }

            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            if (textNode.isMissingNode() || !textNode.isTextual()) {
                throw new IllegalStateException("No text content found in Gemini response candidate");
            }

            return textNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response payload: " + e.getMessage(), e);
        }
    }
}

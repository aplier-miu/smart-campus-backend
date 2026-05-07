package com.example.smartcampus.service;

import com.example.smartcampus.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmClient {

    private final AiProperties props;

    public String chat(String systemPrompt, String userPrompt) {
        WebClient client = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = Map.of(
                "model", props.getModel(),
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        Map<?, ?> resp = client.post()
                .uri("/api/chat")
                .bodyValue(body)
                .retrieve()
                .onStatus(s -> s.isError(), r ->
                        r.bodyToMono(String.class).map(msg -> new RuntimeException("OLLAMA " + r.statusCode() + " => " + msg)))
                .bodyToMono(Map.class)
                .block(java.time.Duration.ofSeconds(props.getTimeoutSeconds()));

        if (resp == null) return null;
        Object msgObj = resp.get("message");
        if (!(msgObj instanceof Map<?,?> msg)) return null;
        Object content = msg.get("content");
        return content == null ? null : String.valueOf(content);
    }
}
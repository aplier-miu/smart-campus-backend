package com.example.smartcampus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String mode = "rule"; // rule / llm
    private String baseUrl = "https://api.openai.com";
    private String apiKey;
    private String model = "gpt-4o-mini";
    private int timeoutSeconds = 20;
}
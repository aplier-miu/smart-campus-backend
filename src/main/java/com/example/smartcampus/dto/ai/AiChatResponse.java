package com.example.smartcampus.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiChatResponse {
    private String answer;
    private List<String> evidence;
    private List<String> actionPlan;
}
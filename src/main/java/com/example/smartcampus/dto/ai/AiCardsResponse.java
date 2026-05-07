package com.example.smartcampus.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiCardsResponse {
    private List<AiCardItem> cards;
}
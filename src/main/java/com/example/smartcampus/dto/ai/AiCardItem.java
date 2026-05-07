package com.example.smartcampus.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiCardItem {
    private String type;      // risk / advice / course
    private String title;
    private String content;
    private String level;     // high / medium / low
}
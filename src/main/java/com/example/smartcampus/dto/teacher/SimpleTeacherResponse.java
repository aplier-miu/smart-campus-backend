package com.example.smartcampus.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleTeacherResponse {
    private Long id;
    private String username;
}
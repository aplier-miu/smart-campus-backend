package com.example.smartcampus.dto.teachingclass;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeachingClassStudentResponse {
    private Long studentId;
    private String username;
    private String email;
}
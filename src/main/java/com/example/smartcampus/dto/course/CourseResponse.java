package com.example.smartcampus.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private int credits;
    private String departmentName;
    private LocalDateTime createdAt;
}
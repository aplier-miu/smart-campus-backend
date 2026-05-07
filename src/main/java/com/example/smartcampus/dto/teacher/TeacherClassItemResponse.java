package com.example.smartcampus.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherClassItemResponse {
    private Long teachingClassId;
    private String classCode;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String semester;
    private Integer capacity;
    private Integer currentStudents;
    private String scheduleSummary;
}
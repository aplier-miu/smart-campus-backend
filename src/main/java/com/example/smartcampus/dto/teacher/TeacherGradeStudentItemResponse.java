package com.example.smartcampus.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherGradeStudentItemResponse {
    private Long studentId;
    private String studentUsername;
    private Long teachingClassId;
    private String classCode;
    private String courseName;
    private String semester;
    private Double score;
    private String gradeType; // MIDTERM/FINAL/ASSIGNMENT/QUIZ
}
package com.example.smartcampus.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentGradeItemResponse {
    private Long gradeId;
    private Long teachingClassId;
    private String classCode;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private String semester;
    private String gradeType;   // MIDTERM / FINAL / ASSIGNMENT / QUIZ
    private Double score;
}
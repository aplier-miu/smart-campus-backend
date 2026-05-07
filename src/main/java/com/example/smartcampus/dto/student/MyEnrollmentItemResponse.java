package com.example.smartcampus.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyEnrollmentItemResponse {
    private Long teachingClassId;
    private String classCode;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private String semester;
}
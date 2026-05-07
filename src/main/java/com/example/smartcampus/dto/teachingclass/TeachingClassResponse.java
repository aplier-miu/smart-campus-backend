package com.example.smartcampus.dto.teachingclass;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeachingClassResponse {
    private Long id;
    private String classCode;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String semester;
    private Integer maxStudents;
    private Integer currentStudents;
    private Integer capacity;
    private LocalDateTime createdAt;

    // 新增：排课摘要（学生选课页展示）
    private String scheduleSummary;

    // 兼容旧构造
    public TeachingClassResponse(Long id, String classCode, Long courseId, String courseCode, String courseName,
                                 Long teacherId, String teacherName, String semester,
                                 Integer maxStudents, Integer currentStudents, Integer capacity,
                                 LocalDateTime createdAt) {
        this(id, classCode, courseId, courseCode, courseName, teacherId, teacherName, semester,
                maxStudents, currentStudents, capacity, createdAt, "");
    }

    // 新构造（推荐）
    public TeachingClassResponse(Long id, String classCode, Long courseId, String courseCode, String courseName,
                                 Long teacherId, String teacherName, String semester,
                                 Integer maxStudents, Integer currentStudents, Integer capacity,
                                 LocalDateTime createdAt, String scheduleSummary) {
        this.id = id;
        this.classCode = classCode;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.semester = semester;
        this.maxStudents = maxStudents;
        this.currentStudents = currentStudents;
        this.capacity = capacity;
        this.createdAt = createdAt;
        this.scheduleSummary = scheduleSummary;
    }
}
package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teaching_classes")
public class TeachingClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code", nullable = false, unique = true, length = 64)
    private String classCode;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "teacher_name", nullable = false, length = 64)
    private String teacherName;

    @Column(name = "semester", nullable = false, length = 32)
    private String semester;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(name = "current_students", nullable = false)
    private Integer currentStudents;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
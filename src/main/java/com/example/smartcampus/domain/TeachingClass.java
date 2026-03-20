package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教学班表实体类，对应 teaching_classes 表
 */
@Entity
@Table(name = "teaching_classes")
@Data
public class TeachingClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 关联课程表

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher; // 关联教师用户（users表）

    @Column(name = "class_code", nullable = false, unique = true)
    private String classCode; // 教学班编号

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents = 50; // 最大学生人数（默认50）

    @Column(name = "current_students")
    private Integer currentStudents = 0; // 当前已选学生人数

    @Column(nullable = false)
    private String semester; // 学期，例 "2026-Spring"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间
}
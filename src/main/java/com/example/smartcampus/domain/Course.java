package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程表实体类，对应 courses 表
 */
@Entity
@Table(name = "courses")
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode; // 课程代码，唯一

    @Column(name = "course_name", nullable = false)
    private String courseName; // 课程名称

    @Lob
    private String description; // 课程详细描述

    @Column(nullable = false)
    private int credits; // 学分数

    @Column(name = "department_name", nullable = false)
    private String departmentName; // 课程所属部门名称

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间
}
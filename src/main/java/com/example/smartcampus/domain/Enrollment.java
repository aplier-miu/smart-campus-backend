package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课表实体类，对应 enrollments 表
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "class_id"}) // 防止重复选课
})
@Data
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student; // 关联学生用户

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private TeachingClass teachingClass; // 关联教学班表

    @Column(name = "enroll_time", nullable = false, updatable = false, insertable = false)
    private LocalDateTime enrollTime;
}
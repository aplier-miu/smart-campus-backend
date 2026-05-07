package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "class_id", "grade_type"})
        }
)
@Data
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student; // 关联学生表

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private TeachingClass teachingClass; // 关联教学班表

    @Column(nullable = false)
    private Double score;

    @Column(name = "grade_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private GradeType gradeType; // 成绩类型（期中，期末等）

    public enum GradeType {
        MIDTERM,
        FINAL,
        ASSIGNMENT,
        QUIZ
    }
}
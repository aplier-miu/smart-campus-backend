package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 成绩表仓储接口
 */
public interface GradeRepository extends JpaRepository<Grade, Long> {

    // 根据学生ID获取成绩
    List<Grade> findByStudent_Id(Long studentId);

    // 根据教学班ID获取成绩
    List<Grade> findByTeachingClass_Id(Long classId);
}
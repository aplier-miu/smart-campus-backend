package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 选课表仓储接口
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 根据学生ID查询选课列表
    List<Enrollment> findByStudent_Id(Long studentId);

    // 根据教学班ID查询选课列表
    List<Enrollment> findByTeachingClass_Id(Long teachingClassId);
}
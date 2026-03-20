package com.example.smartcampus.repository;

import com.example.smartcampus.domain.TeachingClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 教学班表仓储接口
 */
public interface TeachingClassRepository extends JpaRepository<TeachingClass, Long> {

    // 根据课程ID查找所有教学班
    List<TeachingClass> findByCourse_Id(Long courseId);

    // 根据教师ID查找教学班
    List<TeachingClass> findByTeacher_Id(Long teacherId);
}
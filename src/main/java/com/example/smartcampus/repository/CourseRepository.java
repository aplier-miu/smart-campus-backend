package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 课程表仓储接口
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 根据课程编码查询课程
    Optional<Course> findByCourseCode(String courseCode);
}
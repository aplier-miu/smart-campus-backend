package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudent_Id(Long studentId);

    List<Grade> findByTeachingClass_Id(Long classId);

    Optional<Grade> findByStudent_IdAndTeachingClass_IdAndGradeType(Long studentId, Long classId, Grade.GradeType gradeType);
}
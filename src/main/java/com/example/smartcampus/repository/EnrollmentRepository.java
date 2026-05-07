package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudent_IdAndTeachingClass_Id(Long studentId, Long teachingClassId);

    Optional<Enrollment> findByStudent_IdAndTeachingClass_Id(Long studentId, Long teachingClassId);

    List<Enrollment> findByStudent_Id(Long studentId);
    List<Enrollment> findByTeachingClass_Id(Long teachingClassId);
}
package com.example.smartcampus.repository;

import com.example.smartcampus.domain.TeachingClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeachingClassRepository extends JpaRepository<TeachingClass, Long> {
    Optional<TeachingClass> findByClassCode(String classCode);
    List<TeachingClass> findByCourseId(Long courseId);
    List<TeachingClass> findByTeacherId(Long teacherId);

    // 并发保护：选课时锁定该教学班记录（select ... for update）
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TeachingClass t where t.id = :id")
    Optional<TeachingClass> findByIdForUpdate(@Param("id") Long id);
}
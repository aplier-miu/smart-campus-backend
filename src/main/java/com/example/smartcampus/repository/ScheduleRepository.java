package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 排课表仓储接口
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 根据教学班ID查询排课表
    List<Schedule> findByTeachingClass_Id(Long teachingClassId);

    // 根据教室ID查询排课
    List<Schedule> findByClassroom_Id(Long roomId);
}
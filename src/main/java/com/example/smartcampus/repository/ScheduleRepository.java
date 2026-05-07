package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByTeachingClass_Id(Long teachingClassId);

    List<Schedule> findByDayOfWeekAndTimeSlotAndClassroom_Id(Integer dayOfWeek, String timeSlot, Long roomId);

    List<Schedule> findByDayOfWeekAndTimeSlotAndTeachingClass_TeacherId(Integer dayOfWeek, String timeSlot, Long teacherId);

    List<Schedule> findByDayOfWeekAndTimeSlotAndTeachingClass_Id(Integer dayOfWeek, String timeSlot, Long teachingClassId);

    List<Schedule> findByTeachingClass_IdIn(List<Long> teachingClassIds);
}
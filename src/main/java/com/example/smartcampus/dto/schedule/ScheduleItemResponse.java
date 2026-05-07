package com.example.smartcampus.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduleItemResponse {
    private Long id;
    private Long teachingClassId;
    private String classCode;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;

    private Integer dayOfWeek;
    private String timeSlot;

    private Long roomId;
    private String roomNumber;
    private String buildingName;

    private Integer startWeek;
    private Integer endWeek;
}
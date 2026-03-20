package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 排课表实体类，对应 schedules 表
 */
@Entity
@Table(name = "schedules")
@Data
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private TeachingClass teachingClass; // 关联教学班表

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 星期几（1=周一，2=周二......7=周日）

    @Column(name = "time_slot", nullable = false)
    private String timeSlot; // 上课时间段（例："08:00-10:00"）

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Classroom classroom; // 教室表的ID

    @Column(name = "start_week", nullable = false)
    private Integer startWeek; // 开始周数

    @Column(name = "end_week", nullable = false)
    private Integer endWeek; // 结束周数
}
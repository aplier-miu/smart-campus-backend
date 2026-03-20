package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 教室表实体类，对应 classrooms 表
 */
@Entity
@Table(name = "classrooms")
@Data
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber; // 教室编号，唯一

    @Column(name = "building_name")
    private String buildingName; // 教室所属楼栋

    @Column(nullable = false)
    private Integer capacity; // 教室容量（人数限制）

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true; // 教室是否可用

    private String description; // 备注/描述
}
package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 教室表仓储接口
 */
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    // 根据教室编号查找教室
    Classroom findByRoomNumber(String roomNumber);

    // 查找可用的教室列表
    List<Classroom> findByIsAvailableTrue();
}
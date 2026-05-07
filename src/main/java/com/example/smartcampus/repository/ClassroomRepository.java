package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Classroom findByRoomNumber(String roomNumber);

    List<Classroom> findByIsAvailableTrue();

    Page<Classroom> findByRoomNumberLikeOrBuildingNameLike(String roomNumber, String buildingName, Pageable pageable);
}
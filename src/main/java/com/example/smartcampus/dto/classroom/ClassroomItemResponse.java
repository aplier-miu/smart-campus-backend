package com.example.smartcampus.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassroomItemResponse {
    private Long id;
    private String roomNumber;
    private String buildingName;
    private Integer capacity;
    private Boolean isAvailable;
    private String description;
}
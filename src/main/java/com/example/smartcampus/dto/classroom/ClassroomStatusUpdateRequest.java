package com.example.smartcampus.dto.classroom;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassroomStatusUpdateRequest {
    @NotNull(message = "isAvailable不能为空")
    private Boolean isAvailable;
}
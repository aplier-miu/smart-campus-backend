package com.example.smartcampus.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollRequest {
    @NotNull(message = "teachingClassId 不能为空")
    private Long teachingClassId;
}
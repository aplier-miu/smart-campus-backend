package com.example.smartcampus.dto.classroom;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassroomUpdateRequest {

    @NotBlank(message = "教室编号不能为空")
    private String roomNumber;

    private String buildingName;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;

    @NotNull(message = "可用状态不能为空")
    private Boolean isAvailable;

    private String description;
}
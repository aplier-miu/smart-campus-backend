package com.example.smartcampus.dto.teachingclass;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTeachingClassRequest {

    @NotBlank(message = "教学班编号不能为空")
    private String classCode;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于等于1")
    private Integer capacity;
}
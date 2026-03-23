package com.example.smartcampus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "课程代码不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String description;

    @Min(value = 1, message = "学分必须大于等于1")
    private int credits;

    @NotBlank(message = "开课院系不能为空")
    private String departmentName;
}
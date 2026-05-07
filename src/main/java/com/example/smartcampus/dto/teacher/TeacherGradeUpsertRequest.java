package com.example.smartcampus.dto.teacher;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherGradeUpsertRequest {

    @NotNull(message = "studentId不能为空")
    private Long studentId;

    @NotNull(message = "teachingClassId不能为空")
    private Long teachingClassId;

    @NotNull(message = "score不能为空")
    @Min(value = 0, message = "score不能小于0")
    @Max(value = 100, message = "score不能大于100")
    private Double score;

    @NotNull(message = "gradeType不能为空")
    private String gradeType; // MIDTERM/FINAL/ASSIGNMENT/QUIZ
}
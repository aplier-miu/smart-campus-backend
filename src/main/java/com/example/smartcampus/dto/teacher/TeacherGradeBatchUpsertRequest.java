package com.example.smartcampus.dto.teacher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TeacherGradeBatchUpsertRequest {

    @NotEmpty(message = "成绩列表不能为空")
    @Valid
    private List<TeacherGradeUpsertRequest> items;
}
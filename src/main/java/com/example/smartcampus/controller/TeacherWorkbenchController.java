package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.teacher.TeacherClassItemResponse;
import com.example.smartcampus.dto.teacher.TeacherGradeBatchUpsertRequest;
import com.example.smartcampus.dto.teacher.TeacherGradeStudentItemResponse;
import com.example.smartcampus.dto.teacher.TeacherGradeUpsertRequest;
import com.example.smartcampus.service.TeacherWorkbenchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherWorkbenchController {

    private final TeacherWorkbenchService teacherWorkbenchService;

    @GetMapping("/teaching-classes/me")
    public ApiResponse<List<TeacherClassItemResponse>> myClasses(Authentication authentication) {
        return ApiResponse.ok("查询成功", teacherWorkbenchService.listMyTeachingClasses(authentication));
    }

    @GetMapping("/grades")
    public ApiResponse<List<TeacherGradeStudentItemResponse>> classGrades(
            Authentication authentication,
            @RequestParam Long teachingClassId,
            @RequestParam(defaultValue = "FINAL") String gradeType
    ) {
        return ApiResponse.ok("查询成功", teacherWorkbenchService.listStudentsWithGrades(authentication, teachingClassId, gradeType));
    }

    @PostMapping("/grades")
    public ApiResponse<Void> upsertGrade(Authentication authentication, @Valid @RequestBody TeacherGradeUpsertRequest request) {
        teacherWorkbenchService.upsertGrade(authentication, request);
        return ApiResponse.ok("保存成绩成功", null);
    }

    @PostMapping("/grades/batch")
    public ApiResponse<Void> batchUpsertGrades(Authentication authentication, @Valid @RequestBody TeacherGradeBatchUpsertRequest request) {
        teacherWorkbenchService.batchUpsertGrades(authentication, request.getItems());
        return ApiResponse.ok("批量保存成绩成功", null);
    }
}
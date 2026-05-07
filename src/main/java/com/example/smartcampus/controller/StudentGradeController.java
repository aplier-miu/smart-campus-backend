package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.student.StudentGradeItemResponse;
import com.example.smartcampus.service.StudentGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/grades")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentGradeController {

    private final StudentGradeService studentGradeService;

    @GetMapping("/me")
    public ApiResponse<List<StudentGradeItemResponse>> myGrades(
            Authentication authentication,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String gradeType
    ) {
        return ApiResponse.ok("查询成功", studentGradeService.myGrades(authentication, semester, gradeType));
    }
}
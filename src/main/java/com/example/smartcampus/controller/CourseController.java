package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.CourseResponse;
import com.example.smartcampus.dto.CreateCourseRequest;
import com.example.smartcampus.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<List<CourseResponse>> list() {
        return ApiResponse.ok("查询成功", courseService.listCourses());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        return ApiResponse.ok("创建课程成功", courseService.createCourse(request));
    }
}
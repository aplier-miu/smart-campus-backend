package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.course.CourseResponse;
import com.example.smartcampus.dto.course.CreateCourseRequest;
import com.example.smartcampus.dto.course.UpdateCourseRequest;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody UpdateCourseRequest request) {
        return ApiResponse.ok("更新课程成功", courseService.updateCourse(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.ok("删除课程成功", null);
    }
}
package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.student.EnrollRequest;
import com.example.smartcampus.dto.student.MyEnrollmentItemResponse;
import com.example.smartcampus.service.StudentEnrollmentService;
import com.example.smartcampus.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentEnrollmentController {

    private final StudentEnrollmentService studentEnrollmentService;
    private final JwtUtil jwtUtil;
    private Long currentUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("缺少或非法 Authorization");
        }
        String token = auth.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) throw new RuntimeException("token 中缺少 userId");
        return userId;
    }
    @GetMapping("/me")
    public ApiResponse<List<MyEnrollmentItemResponse>> my(HttpServletRequest request) {
        Long studentId = currentUserId(request);
        return ApiResponse.ok("查询成功", studentEnrollmentService.myEnrollments(studentId));
    }

    @PostMapping
    public ApiResponse<Void> enroll(@Valid @RequestBody EnrollRequest req, HttpServletRequest request) {
        Long studentId = currentUserId(request);
        studentEnrollmentService.enroll(studentId, req);
        return ApiResponse.ok("选课成功", null);
    }

    @DeleteMapping("/{teachingClassId}")
    public ApiResponse<Void> drop(@PathVariable Long teachingClassId, HttpServletRequest request) {
        Long studentId = currentUserId(request);
        studentEnrollmentService.drop(studentId, teachingClassId);
        return ApiResponse.ok("退课成功", null);
    }
}
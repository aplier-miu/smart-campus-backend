package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.teacher.SimpleTeacherResponse;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<SimpleTeacherResponse>> listTeachers() {
        List<SimpleTeacherResponse> data = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "TEACHER".equalsIgnoreCase(u.getRole().toString()))
                .map(u -> new SimpleTeacherResponse(u.getId(), u.getUsername()))
                .toList();
        return ApiResponse.ok("查询成功", data);
    }
}
package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.ok("访问成功", "Hello SmartCampus");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/test/admin")
    public ApiResponse<String> adminOnly() {
        return ApiResponse.ok("访问成功", "Hello Admin");
    }
}
package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.common.DashboardStatsResponse;
import com.example.smartcampus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> stats() {
        return ApiResponse.ok("查询成功", dashboardService.getStats());
    }
}
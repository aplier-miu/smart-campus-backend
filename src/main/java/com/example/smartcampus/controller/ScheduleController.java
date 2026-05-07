package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.schedule.CreateScheduleRequest;
import com.example.smartcampus.dto.schedule.ScheduleItemResponse;
import com.example.smartcampus.dto.schedule.UpdateScheduleRequest;
import com.example.smartcampus.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ApiResponse<List<ScheduleItemResponse>> list(@RequestParam(required = false) Long teachingClassId) {
        return ApiResponse.ok("查询成功", scheduleService.list(teachingClassId));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateScheduleRequest req) {
        scheduleService.create(req);
        return ApiResponse.ok("新增排课成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateScheduleRequest req) {
        scheduleService.update(id, req);
        return ApiResponse.ok("修改排课成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ApiResponse.ok("删除排课成功", null);
    }
}
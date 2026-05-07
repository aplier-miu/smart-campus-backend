package com.example.smartcampus.controller;

import com.example.smartcampus.domain.Classroom;
import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.classroom.ClassroomCreateRequest;
import com.example.smartcampus.dto.classroom.ClassroomItemResponse;
import com.example.smartcampus.dto.classroom.ClassroomStatusUpdateRequest;
import com.example.smartcampus.dto.classroom.ClassroomUpdateRequest;
import com.example.smartcampus.repository.ClassroomRepository;
import com.example.smartcampus.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomRepository classroomRepository;
    private final ClassroomService classroomService;

    /**
     * 给排课下拉使用（默认全部；onlyAvailable=true 时只返回可用教室）
     */
    @GetMapping
    public ApiResponse<List<Classroom>> list(@RequestParam(required = false) Boolean onlyAvailable) {
        if (Boolean.TRUE.equals(onlyAvailable)) {
            return ApiResponse.ok("查询成功", classroomRepository.findByIsAvailableTrue());
        }
        return ApiResponse.ok("查询成功", classroomRepository.findAll());
    }

    /**
     * 教室管理分页查询（管理员）
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ClassroomItemResponse>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        return classroomService.page(page, size, keyword);
    }

    /**
     * 新增教室（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> create(@Valid @RequestBody ClassroomCreateRequest req) {
        return classroomService.create(req);
    }

    /**
     * 修改教室（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ClassroomUpdateRequest req) {
        return classroomService.update(id, req);
    }

    /**
     * 启用/禁用教室（管理员）
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ClassroomStatusUpdateRequest req) {
        return classroomService.updateStatus(id, req);
    }

    /**
     * 删除教室（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return classroomService.delete(id);
    }
}
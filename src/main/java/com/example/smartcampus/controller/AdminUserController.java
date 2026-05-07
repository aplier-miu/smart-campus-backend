package com.example.smartcampus.controller;

import com.example.smartcampus.dto.admin.AdminUserItemResponse;
import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.admin.CreateUserByAdminRequest;
import com.example.smartcampus.dto.admin.UpdateUserStatusRequest;
import com.example.smartcampus.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<List<AdminUserItemResponse>> list(@RequestParam String role) {
        return ApiResponse.ok("查询成功", adminUserService.listByRole(role));
    }

    @PostMapping
    public ApiResponse<AdminUserItemResponse> create(@Valid @RequestBody CreateUserByAdminRequest req) {
        return ApiResponse.ok("创建成功，初始密码为123456", adminUserService.create(req));
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return ApiResponse.ok("密码已重置为123456", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @RequestBody UpdateUserStatusRequest req) {
        adminUserService.updateStatus(id, req.getStatus());
        return ApiResponse.ok("状态更新成功", null);
    }
}
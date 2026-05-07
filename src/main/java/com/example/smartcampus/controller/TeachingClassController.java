package com.example.smartcampus.controller;

import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.teachingclass.CreateTeachingClassRequest;
import com.example.smartcampus.dto.teachingclass.TeachingClassResponse;
import com.example.smartcampus.dto.teachingclass.TeachingClassStudentResponse;
import com.example.smartcampus.dto.teachingclass.UpdateTeachingClassRequest;
import com.example.smartcampus.service.TeachingClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/teaching-classes")
@RequiredArgsConstructor
public class TeachingClassController {

    private final TeachingClassService teachingClassService;

    @GetMapping
    public ApiResponse<List<TeachingClassResponse>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyScheduled
    ) {
        return ApiResponse.ok("查询成功", teachingClassService.list(courseId, onlyScheduled));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<TeachingClassResponse> create(@Valid @RequestBody CreateTeachingClassRequest request) {
        return ApiResponse.ok("创建教学班成功", teachingClassService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<TeachingClassResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateTeachingClassRequest request) {
        return ApiResponse.ok("更新教学班成功", teachingClassService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teachingClassService.delete(id);
        return ApiResponse.ok("删除教学班成功", null);
    }

    @GetMapping("/{id}/students")
    public ApiResponse<List<TeachingClassStudentResponse>> listStudents(@PathVariable Long id) {
        return ApiResponse.ok("查询成功", teachingClassService.listStudents(id));
    }

    @GetMapping("/{id}/students/export")
    public ResponseEntity<byte[]> exportStudents(@PathVariable Long id) {
        byte[] csv = teachingClassService.exportStudentsCsv(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "roster-" + id + ".csv");
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));

        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
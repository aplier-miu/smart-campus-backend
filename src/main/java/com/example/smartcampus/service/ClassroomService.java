package com.example.smartcampus.service;

import com.example.smartcampus.domain.Classroom;
import com.example.smartcampus.dto.classroom.ClassroomCreateRequest;
import com.example.smartcampus.dto.classroom.ClassroomItemResponse;
import com.example.smartcampus.dto.classroom.ClassroomStatusUpdateRequest;
import com.example.smartcampus.dto.classroom.ClassroomUpdateRequest;
import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ApiResponse<Page<ClassroomItemResponse>> page(Integer page, Integer size, String keyword) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;

        Pageable pageable = PageRequest.of(p - 1, s, Sort.by(Sort.Direction.DESC, "id"));
        Page<Classroom> result;

        if (keyword == null || keyword.trim().isEmpty()) {
            result = classroomRepository.findAll(pageable);
        } else {
            String kw = "%" + keyword.trim() + "%";
            result = classroomRepository.findByRoomNumberLikeOrBuildingNameLike(kw, kw, pageable);
        }

        Page<ClassroomItemResponse> mapped = result.map(this::toResp);
        return ApiResponse.ok("查询成功", mapped);
    }

    @Transactional
    public ApiResponse<Void> create(ClassroomCreateRequest req) {
        Classroom exists = classroomRepository.findByRoomNumber(req.getRoomNumber().trim());
        if (exists != null) throw new BusinessException("教室编号已存在");

        Classroom c = new Classroom();
        c.setRoomNumber(req.getRoomNumber().trim());
        c.setBuildingName(trimOrNull(req.getBuildingName()));
        c.setCapacity(req.getCapacity());
        c.setIsAvailable(req.getIsAvailable());
        c.setDescription(trimOrNull(req.getDescription()));

        classroomRepository.save(c);
        return ApiResponse.ok("新增教室成功", null);
    }

    @Transactional
    public ApiResponse<Void> update(Long id, ClassroomUpdateRequest req) {
        Classroom c = classroomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教室不存在"));

        Classroom exists = classroomRepository.findByRoomNumber(req.getRoomNumber().trim());
        if (exists != null && !exists.getId().equals(id)) {
            throw new BusinessException("教室编号已存在");
        }

        c.setRoomNumber(req.getRoomNumber().trim());
        c.setBuildingName(trimOrNull(req.getBuildingName()));
        c.setCapacity(req.getCapacity());
        c.setIsAvailable(req.getIsAvailable());
        c.setDescription(trimOrNull(req.getDescription()));

        classroomRepository.save(c);
        return ApiResponse.ok("修改教室成功", null);
    }

    @Transactional
    public ApiResponse<Void> updateStatus(Long id, ClassroomStatusUpdateRequest req) {
        Classroom c = classroomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教室不存在"));
        c.setIsAvailable(req.getIsAvailable());
        classroomRepository.save(c);
        return ApiResponse.ok("状态更新成功", null);
    }

    @Transactional
    public ApiResponse<Void> delete(Long id) {
        Classroom c = classroomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教室不存在"));
        classroomRepository.delete(c);
        return ApiResponse.ok("删除成功", null);
    }

    private ClassroomItemResponse toResp(Classroom c) {
        return new ClassroomItemResponse(
                c.getId(),
                c.getRoomNumber(),
                c.getBuildingName(),
                c.getCapacity(),
                c.getIsAvailable(),
                c.getDescription()
        );
    }

    private String trimOrNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
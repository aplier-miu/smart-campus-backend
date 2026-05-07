package com.example.smartcampus.service;

import com.example.smartcampus.domain.User;
import com.example.smartcampus.domain.UserStatus;
import com.example.smartcampus.dto.admin.AdminUserItemResponse;
import com.example.smartcampus.dto.admin.CreateUserByAdminRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUserItemResponse> listByRole(String role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && role.equalsIgnoreCase(u.getRole().name()))
                .map(this::toResp)
                .toList();
    }

    public AdminUserItemResponse create(CreateUserByAdminRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已存在");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode("123456"));

        try {
            u.setRole(User.Role.valueOf(req.getRole()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("角色只能是 STUDENT 或 TEACHER");
        }

        u.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(u);
        return toResp(saved);
    }

    public void resetPassword(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        u.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(u);
    }
    public void updateStatus(Long id, String status) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String normalized = status == null ? "" : status.trim().toUpperCase();

        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new BusinessException("状态非法，只能是 ACTIVE 或 INACTIVE");
        }

        u.setStatus(UserStatus.valueOf(normalized));
        userRepository.save(u);
    }

    private AdminUserItemResponse toResp(User u) {
        return new AdminUserItemResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole() == null ? "" : u.getRole().name(),
                u.getStatus() == null ? "" : u.getStatus().name()
        );
    }
}
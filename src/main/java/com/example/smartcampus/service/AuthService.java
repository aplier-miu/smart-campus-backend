package com.example.smartcampus.service;

import com.example.smartcampus.domain.User;
import com.example.smartcampus.domain.UserStatus;
import com.example.smartcampus.dto.LoginRequest;
import com.example.smartcampus.dto.RegisterRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.smartcampus.dto.LoginResponse;
import com.example.smartcampus.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() == null ? User.Role.STUDENT : request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
        return "注册成功";
    }

    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("账号不可用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, "Bearer");
    }
}
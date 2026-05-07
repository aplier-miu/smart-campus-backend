package com.example.smartcampus.service;

import com.example.smartcampus.domain.User;
import com.example.smartcampus.domain.UserStatus;
import com.example.smartcampus.dto.auth.LoginRequest;
import com.example.smartcampus.dto.auth.LoginResponse;
import com.example.smartcampus.dto.auth.RegisterRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.exception.ErrorCode;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;

    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_MILLIS = 10 * 60 * 1000L; // 10分钟

    private final Map<String, FailRecord> failCache = new ConcurrentHashMap<>();

    private static class FailRecord {
        int failCount;
        long lockUntilEpochMs;
        long lastFailEpochMs;
    }

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

    public LoginResponse login(LoginRequest request) {
        String username = safe(request.getUsername()).trim();
        String password = safe(request.getPassword());
        String captchaId = safe(request.getCaptchaId()).trim();
        String captchaCode = safe(request.getCaptchaCode()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码不能为空");
        }
        if (captchaId.isEmpty() || captchaCode.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不能为空");
        }

        String key = username.toLowerCase();
        long now = Instant.now().toEpochMilli();

        FailRecord record = failCache.get(key);
        if (record != null && record.lockUntilEpochMs > now) {
            long sec = (record.lockUntilEpochMs - now + 999) / 1000;
            throw new BusinessException(ErrorCode.LOGIN_LOCKED, "登录失败次数过多，请 " + sec + " 秒后重试");
        }

        try {
            captchaService.verifyAndConsume(captchaId, captchaCode);
        } catch (BusinessException e) {
            onLoginFailed(key);
            throw e;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    onLoginFailed(key);
                    return new BusinessException(ErrorCode.LOGIN_CREDENTIAL_INVALID, "用户名或密码错误");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            onLoginFailed(key);
            throw new BusinessException(ErrorCode.LOGIN_CREDENTIAL_INVALID, "用户名或密码错误");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账号不可用，请联系管理员");
        }

        failCache.remove(key);

        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, "Bearer");
    }

    private void onLoginFailed(String key) {
        long now = Instant.now().toEpochMilli();
        failCache.compute(key, (k, old) -> {
            FailRecord r = (old == null) ? new FailRecord() : old;

            if (r.lockUntilEpochMs > 0 && r.lockUntilEpochMs <= now) {
                r.failCount = 0;
                r.lockUntilEpochMs = 0;
            }

            r.failCount++;
            r.lastFailEpochMs = now;

            if (r.failCount >= MAX_FAIL_COUNT) {
                r.lockUntilEpochMs = now + LOCK_MILLIS;
                r.failCount = 0;
            }
            return r;
        });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
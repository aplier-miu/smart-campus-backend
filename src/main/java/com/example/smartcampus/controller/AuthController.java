package com.example.smartcampus.controller;

import com.example.smartcampus.dto.auth.CaptchaResponse;
import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.dto.auth.LoginRequest;
import com.example.smartcampus.dto.auth.LoginResponse;
import com.example.smartcampus.dto.auth.RegisterRequest;
import com.example.smartcampus.service.AuthService;
import com.example.smartcampus.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        var data = captchaService.generate();
        return ApiResponse.ok("获取验证码成功", new CaptchaResponse(data.captchaId(), data.imageBase64()));
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        String msg = authService.register(request);
        return ApiResponse.ok(msg, null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = authService.login(request);
        return ApiResponse.ok("登录成功", data);
    }
}
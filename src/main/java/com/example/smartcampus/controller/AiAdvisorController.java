package com.example.smartcampus.controller;

import com.example.smartcampus.domain.User;
import com.example.smartcampus.dto.ai.*;
import com.example.smartcampus.dto.common.ApiResponse;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.service.AiAdvisorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;
    private final UserRepository userRepository;

    private Long currentUserId(Authentication authentication) {
        String username = authentication.getName(); // 这里是 student001
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return user.getId();
    }

    @GetMapping("/cards/me")
    public ApiResponse<AiCardsResponse> myCards(Authentication authentication) {
        Long studentId = currentUserId(authentication);
        return ApiResponse.ok("获取成功", new AiCardsResponse(aiAdvisorService.buildCards(studentId)));
    }

    @PostMapping("/chat/me")
    public ApiResponse<AiChatResponse> myChat(Authentication authentication, @Valid @RequestBody AiChatRequest req) {
        Long studentId = currentUserId(authentication);
        return ApiResponse.ok("回答成功", aiAdvisorService.chat(studentId, req.getQuestion()));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("AI服务正常", "ok");
    }
}
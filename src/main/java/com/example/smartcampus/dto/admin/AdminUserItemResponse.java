package com.example.smartcampus.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserItemResponse {
    private Long id;
    private String username;
    private String email;
    private String role;   // 返回给前端字符串
    private String status; // 返回给前端字符串
}
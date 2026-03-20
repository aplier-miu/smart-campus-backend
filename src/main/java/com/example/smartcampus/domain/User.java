package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表实体类，对应 users 数据库表
 */
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(nullable = false, unique = true)
    private String username; // 用户名，唯一

    @Column(nullable = false)
    private String password; // 密码

    @Column(nullable = false, unique = true)
    private String email; // 邮箱地址

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 用户角色（学生, 教师, 管理员）

    @Column(nullable = false)
    private String status = "ACTIVE"; // 用户状态（默认 active）

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间

    public enum Role {
        STUDENT, TEACHER, ADMIN // 枚举角色类型
    }
}
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE; // 用户状态

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt; // 创建时间（由数据库默认值维护）

    public enum Role {
        STUDENT, TEACHER, ADMIN
    }
}
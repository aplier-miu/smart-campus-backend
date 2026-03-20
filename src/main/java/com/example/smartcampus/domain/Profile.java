package com.example.smartcampus.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户扩展信息表实体类，对应 profiles 表
 */
@Entity
@Table(name = "profiles")
@Data
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 关联用户表

    @Column(name = "full_name")
    private String fullName; // 用户真实姓���

    private String gender; // 性别，例如 "M" 或 "F"

    @Column(length = 15)
    private String phone; // 联系电话

    private String address; // 住址

    private String avatarUrl; // 用户头像

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间
}
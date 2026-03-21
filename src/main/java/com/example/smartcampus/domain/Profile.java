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
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // 关联用户表（一对一）

    @Column(name = "full_name")
    private String fullName;

    private String gender;

    @Column(length = 15)
    private String phone;

    private String address;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Lob
    private String bio;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
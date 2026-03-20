package com.example.smartcampus.repository;

import com.example.smartcampus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 用户表仓储接口，用于定义数据库操作
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查询用户
    Optional<User> findByUsername(String username);

    // 根据邮箱查询用户
    Optional<User> findByEmail(String email);
}
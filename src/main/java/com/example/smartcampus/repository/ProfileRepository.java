package com.example.smartcampus.repository;

import com.example.smartcampus.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户扩展信息表仓储接口
 */
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // 根据用户ID查找扩展信息
    Profile findByUser_Id(Long userId);
}
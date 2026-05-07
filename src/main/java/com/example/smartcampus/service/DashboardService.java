package com.example.smartcampus.service;

import com.example.smartcampus.dto.common.DashboardStatsResponse;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.TeachingClassRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.smartcampus.domain.User;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TeachingClassRepository teachingClassRepository;

    public DashboardStatsResponse getStats() {
        Long studentCount = userRepository.countByRole(User.Role.STUDENT);
        Long courseCount = courseRepository.count();
        Long teachingClassCount = teachingClassRepository.count();

        return new DashboardStatsResponse(studentCount, courseCount, teachingClassCount);
    }
}
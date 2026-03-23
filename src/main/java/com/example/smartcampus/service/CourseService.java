package com.example.smartcampus.service;

import com.example.smartcampus.domain.Course;
import com.example.smartcampus.dto.CourseResponse;
import com.example.smartcampus.dto.CreateCourseRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.findByCourseCode(request.getCourseCode()).isPresent()) {
            throw new BusinessException("课程代码已存在");
        }

        Course c = new Course();
        c.setCourseCode(request.getCourseCode());
        c.setCourseName(request.getCourseName());
        c.setDescription(request.getDescription());
        c.setCredits(request.getCredits());
        c.setDepartmentName(request.getDepartmentName());

        Course saved = courseRepository.save(c);
        return toResp(saved);
    }

    public List<CourseResponse> listCourses() {
        return courseRepository.findAll().stream().map(this::toResp).toList();
    }

    private CourseResponse toResp(Course c) {
        return new CourseResponse(
                c.getId(),
                c.getCourseCode(),
                c.getCourseName(),
                c.getDescription(),
                c.getCredits(),
                c.getDepartmentName(),
                c.getCreatedAt()
        );
    }
}
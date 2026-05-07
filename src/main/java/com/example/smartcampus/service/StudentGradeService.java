package com.example.smartcampus.service;

import com.example.smartcampus.domain.Course;
import com.example.smartcampus.domain.Grade;
import com.example.smartcampus.domain.TeachingClass;
import com.example.smartcampus.domain.User;
import com.example.smartcampus.dto.student.StudentGradeItemResponse;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.GradeRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentGradeService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;

    public List<StudentGradeItemResponse> myGrades(Authentication authentication, String semester, String gradeType) {
        User student = getCurrentStudent(authentication);

        // 用临时变量解析，再赋给 final 变量供 lambda 使用
        Grade.GradeType parsedTypeTemp = null;
        if (gradeType != null && !gradeType.trim().isEmpty()) {
            try {
                parsedTypeTemp = Grade.GradeType.valueOf(gradeType.trim().toUpperCase());
            } catch (Exception e) {
                throw new BusinessException("gradeType非法，支持：MIDTERM/FINAL/ASSIGNMENT/QUIZ");
            }
        }
        final Grade.GradeType parsedType = parsedTypeTemp;

        final String sem = semester == null ? "" : semester.trim();

        return gradeRepository.findByStudent_Id(student.getId()).stream()
                .filter(g -> parsedType == null || g.getGradeType() == parsedType)
                .map(g -> {
                    TeachingClass tc = g.getTeachingClass();
                    if (tc == null) return null;

                    String inputSem = sem.replace("－", "-").replace("—", "-").trim();
                    String tcSem = (tc.getSemester() == null ? "" : tc.getSemester())
                            .replace("－", "-").replace("—", "-").trim();

                    if (!inputSem.isEmpty() && !tcSem.startsWith(inputSem)) {
                        return null;
                    }

                    Course c = courseRepository.findById(tc.getCourseId()).orElse(null);

                    return new StudentGradeItemResponse(
                            g.getId(),
                            tc.getId(),
                            nvl(tc.getClassCode()),
                            c == null ? "" : nvl(c.getCourseCode()),
                            c == null ? "" : nvl(c.getCourseName()),
                            nvl(tc.getTeacherName()),
                            nvl(tc.getSemester()),
                            g.getGradeType() == null ? "" : g.getGradeType().name(),
                            g.getScore()
                    );
                })
                .filter(x -> x != null)
                .toList();
    }

    private User getCurrentStudent(Authentication authentication) {
        String username = authentication == null ? "" : String.valueOf(authentication.getName());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));
        if (user.getRole() != User.Role.STUDENT) {
            throw new BusinessException("仅学生可访问该功能");
        }
        return user;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
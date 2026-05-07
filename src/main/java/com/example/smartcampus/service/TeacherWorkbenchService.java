package com.example.smartcampus.service;

import com.example.smartcampus.domain.Course;
import com.example.smartcampus.domain.Enrollment;
import com.example.smartcampus.domain.Grade;
import com.example.smartcampus.domain.Schedule;
import com.example.smartcampus.domain.TeachingClass;
import com.example.smartcampus.domain.User;
import com.example.smartcampus.dto.teacher.TeacherClassItemResponse;
import com.example.smartcampus.dto.teacher.TeacherGradeStudentItemResponse;
import com.example.smartcampus.dto.teacher.TeacherGradeUpsertRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.EnrollmentRepository;
import com.example.smartcampus.repository.GradeRepository;
import com.example.smartcampus.repository.ScheduleRepository;
import com.example.smartcampus.repository.TeachingClassRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherWorkbenchService {

    private final UserRepository userRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final CourseRepository courseRepository;
    private final ScheduleRepository scheduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;

    public List<TeacherClassItemResponse> listMyTeachingClasses(Authentication authentication) {
        User teacher = currentTeacher(authentication);
        List<TeachingClass> classes = teachingClassRepository.findByTeacherId(teacher.getId());
        if (classes.isEmpty()) return List.of();

        Map<Long, List<Schedule>> scheduleMap = buildScheduleMap(classes);

        return classes.stream().map(tc -> {
            Course c = courseRepository.findById(tc.getCourseId()).orElse(null);
            return new TeacherClassItemResponse(
                    tc.getId(),
                    tc.getClassCode(),
                    tc.getCourseId(),
                    c == null ? "" : nvl(c.getCourseCode()),
                    c == null ? "" : nvl(c.getCourseName()),
                    nvl(tc.getSemester()),
                    tc.getCapacity(),
                    tc.getCurrentStudents(),
                    buildScheduleSummary(scheduleMap.getOrDefault(tc.getId(), List.of()))
            );
        }).toList();
    }

    public List<TeacherGradeStudentItemResponse> listStudentsWithGrades(Authentication authentication, Long teachingClassId, String gradeType) {
        User teacher = currentTeacher(authentication);
        TeachingClass tc = teachingClassRepository.findById(teachingClassId)
                .orElseThrow(() -> new BusinessException("教学班不存在"));

        ensureTeacherOwnsClass(teacher.getId(), tc);

        Grade.GradeType gt = parseGradeType(gradeType);

        List<Enrollment> enrollments = enrollmentRepository.findByTeachingClass_Id(teachingClassId);
        Course course = courseRepository.findById(tc.getCourseId()).orElse(null);

        List<Grade> gradeList = gradeRepository.findByTeachingClass_Id(teachingClassId).stream()
                .filter(g -> g.getGradeType() == gt)
                .toList();

        Map<Long, Grade> gradeMap = gradeList.stream()
                .collect(Collectors.toMap(g -> g.getStudent().getId(), g -> g, (a, b) -> a));

        return enrollments.stream().map(en -> {
            Long sid = en.getStudent().getId();
            Grade g = gradeMap.get(sid);
            return new TeacherGradeStudentItemResponse(
                    sid,
                    nvl(en.getStudent().getUsername()),
                    tc.getId(),
                    nvl(tc.getClassCode()),
                    course == null ? "" : nvl(course.getCourseName()),
                    nvl(tc.getSemester()),
                    g == null ? null : g.getScore(),
                    gt.name()
            );
        }).toList();
    }

    @Transactional
    public void upsertGrade(Authentication authentication, TeacherGradeUpsertRequest request) {
        internalUpsert(authentication, request);
    }

    @Transactional
    public void batchUpsertGrades(Authentication authentication, List<TeacherGradeUpsertRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("成绩列表不能为空");
        }
        for (TeacherGradeUpsertRequest item : items) {
            internalUpsert(authentication, item);
        }
    }

    private void internalUpsert(Authentication authentication, TeacherGradeUpsertRequest request) {
        User teacher = currentTeacher(authentication);

        TeachingClass tc = teachingClassRepository.findById(request.getTeachingClassId())
                .orElseThrow(() -> new BusinessException("教学班不存在"));
        ensureTeacherOwnsClass(teacher.getId(), tc);

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("学生不存在"));
        if (student.getRole() != User.Role.STUDENT) {
            throw new BusinessException("studentId 对应用户不是学生");
        }

        boolean enrolled = enrollmentRepository.existsByStudent_IdAndTeachingClass_Id(student.getId(), tc.getId());
        if (!enrolled) {
            throw new BusinessException("该学生未选该教学班，不能录入成绩");
        }

        Grade.GradeType gt = parseGradeType(request.getGradeType());

        Grade grade = gradeRepository.findByStudent_IdAndTeachingClass_IdAndGradeType(student.getId(), tc.getId(), gt)
                .orElseGet(Grade::new);

        grade.setStudent(student);
        grade.setTeachingClass(tc);
        grade.setGradeType(gt);
        grade.setScore(request.getScore());

        gradeRepository.save(grade);
    }

    private User currentTeacher(Authentication authentication) {
        String username = authentication == null ? "" : String.valueOf(authentication.getName());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("当前登录用户不存在"));

        if (user.getRole() != User.Role.TEACHER) {
            throw new BusinessException("仅教师可访问该功能");
        }
        return user;
    }

    private void ensureTeacherOwnsClass(Long teacherId, TeachingClass tc) {
        if (!Objects.equals(teacherId, tc.getTeacherId())) {
            throw new BusinessException("无权访问非本人教学班");
        }
    }

    private Grade.GradeType parseGradeType(String gradeType) {
        try {
            return Grade.GradeType.valueOf(gradeType == null ? "" : gradeType.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("gradeType非法，支持：MIDTERM/FINAL/ASSIGNMENT/QUIZ");
        }
    }

    private Map<Long, List<Schedule>> buildScheduleMap(List<TeachingClass> classes) {
        List<Long> ids = classes.stream().map(TeachingClass::getId).toList();
        if (ids.isEmpty()) return Map.of();

        List<Schedule> all = scheduleRepository.findAll().stream()
                .filter(s -> s.getTeachingClass() != null && ids.contains(s.getTeachingClass().getId()))
                .toList();

        return all.stream().collect(Collectors.groupingBy(s -> s.getTeachingClass().getId()));
    }

    private String buildScheduleSummary(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) return "";
        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek).thenComparing(Schedule::getTimeSlot))
                .map(s -> "周" + cnDay(s.getDayOfWeek())
                        + " " + nvl(s.getTimeSlot())
                        + " " + nvl(s.getClassroom() == null ? "" : s.getClassroom().getRoomNumber())
                        + (nvl(s.getClassroom() == null ? "" : s.getClassroom().getBuildingName()).isEmpty()
                        ? ""
                        : "（" + s.getClassroom().getBuildingName() + "）")
                        + " 第" + s.getStartWeek() + "-" + s.getEndWeek() + "周")
                .collect(Collectors.joining("；"));
    }

    private String cnDay(Integer d) {
        if (d == null) return "";
        return switch (d) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            case 6 -> "六";
            case 7 -> "日";
            default -> "";
        };
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
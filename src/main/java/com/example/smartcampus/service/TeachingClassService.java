package com.example.smartcampus.service;

import com.example.smartcampus.domain.Course;
import com.example.smartcampus.domain.Enrollment;
import com.example.smartcampus.domain.Schedule;
import com.example.smartcampus.domain.TeachingClass;
import com.example.smartcampus.domain.User;
import com.example.smartcampus.dto.teachingclass.CreateTeachingClassRequest;
import com.example.smartcampus.dto.teachingclass.TeachingClassResponse;
import com.example.smartcampus.dto.teachingclass.TeachingClassStudentResponse;
import com.example.smartcampus.dto.teachingclass.UpdateTeachingClassRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.EnrollmentRepository;
import com.example.smartcampus.repository.ScheduleRepository;
import com.example.smartcampus.repository.TeachingClassRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingClassService {

    private final TeachingClassRepository teachingClassRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final EnrollmentRepository enrollmentRepository;

    public TeachingClassResponse create(CreateTeachingClassRequest request) {
        if (teachingClassRepository.findByClassCode(request.getClassCode()).isPresent()) {
            throw new BusinessException("教学班编号已存在");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException("教师不存在"));

        String roleText = teacher.getRole() == null ? "" : teacher.getRole().toString();
        if (!"TEACHER".equalsIgnoreCase(roleText)) {
            throw new BusinessException("teacherId 对应用户不是教师角色");
        }

        TeachingClass tc = new TeachingClass();
        tc.setClassCode(request.getClassCode());
        tc.setCourseId(request.getCourseId());
        tc.setTeacherId(request.getTeacherId());
        tc.setTeacherName(teacher.getUsername());
        tc.setSemester(request.getSemester());
        tc.setCapacity(request.getCapacity());
        tc.setMaxStudents(request.getCapacity());
        tc.setCurrentStudents(0);

        TeachingClass saved = teachingClassRepository.save(tc);
        return toResp(saved, course, "");
    }

    public List<TeachingClassResponse> list(Long courseId, Boolean onlyScheduled) {
        List<TeachingClass> list = (courseId == null)
                ? teachingClassRepository.findAll()
                : teachingClassRepository.findByCourseId(courseId);

        Map<Long, List<Schedule>> scheduleMap = buildScheduleMap(list);

        return list.stream()
                .filter(tc -> !Boolean.TRUE.equals(onlyScheduled) || scheduleMap.containsKey(tc.getId()))
                .map(tc -> {
                    Course c = courseRepository.findById(tc.getCourseId()).orElse(null);
                    String summary = buildScheduleSummary(scheduleMap.getOrDefault(tc.getId(), List.of()));
                    return toResp(tc, c, summary);
                })
                .toList();
    }

    public TeachingClassResponse update(Long id, UpdateTeachingClassRequest request) {
        TeachingClass tc = teachingClassRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教学班不存在"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException("教师不存在"));

        String roleText = teacher.getRole() == null ? "" : teacher.getRole().toString();
        if (!"TEACHER".equalsIgnoreCase(roleText)) {
            throw new BusinessException("teacherId 对应用户不是教师角色");
        }

        if (request.getCapacity() < tc.getCurrentStudents()) {
            throw new BusinessException("容量不能小于当前已选人数");
        }

        tc.setCourseId(request.getCourseId());
        tc.setTeacherId(request.getTeacherId());
        tc.setTeacherName(teacher.getUsername());
        tc.setSemester(request.getSemester());
        tc.setCapacity(request.getCapacity());
        tc.setMaxStudents(request.getCapacity());

        TeachingClass saved = teachingClassRepository.save(tc);

        List<Schedule> schedules = scheduleRepository.findByTeachingClass_Id(saved.getId());
        return toResp(saved, course, buildScheduleSummary(schedules));
    }

    public void delete(Long id) {
        TeachingClass tc = teachingClassRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教学班不存在"));
        teachingClassRepository.delete(tc);
    }

    public List<TeachingClassStudentResponse> listStudents(Long teachingClassId) {
        if (!teachingClassRepository.existsById(teachingClassId)) {
            throw new BusinessException("教学班不存在");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByTeachingClass_Id(teachingClassId);
        if (enrollments.isEmpty()) return List.of();

        return enrollments.stream()
                .map(Enrollment::getStudent)
                .filter(Objects::nonNull)
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .distinct()
                .map(u -> new TeachingClassStudentResponse(u.getId(), u.getUsername(), u.getEmail()))
                .toList();
    }

    public byte[] exportStudentsCsv(Long teachingClassId) {
        List<TeachingClassStudentResponse> students = listStudents(teachingClassId);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM
        sb.append("学号ID,用户名,邮箱\n");
        for (TeachingClassStudentResponse s : students) {
            sb.append(csv(s.getStudentId()))
                    .append(",")
                    .append(csv(s.getUsername()))
                    .append(",")
                    .append(csv(s.getEmail()))
                    .append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private Map<Long, List<Schedule>> buildScheduleMap(List<TeachingClass> teachingClasses) {
        if (teachingClasses == null || teachingClasses.isEmpty()) return Map.of();

        List<Long> ids = teachingClasses.stream().map(TeachingClass::getId).toList();
        List<Schedule> schedules = scheduleRepository.findAll().stream()
                .filter(s -> s.getTeachingClass() != null && ids.contains(s.getTeachingClass().getId()))
                .toList();

        return schedules.stream().collect(Collectors.groupingBy(s -> s.getTeachingClass().getId()));
    }

    private String buildScheduleSummary(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) return "";

        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek).thenComparing(Schedule::getTimeSlot))
                .map(s -> "周" + cnDay(s.getDayOfWeek())
                        + " " + safe(s.getTimeSlot())
                        + " " + safe(s.getClassroom() == null ? "" : s.getClassroom().getRoomNumber())
                        + (safe(s.getClassroom() == null ? "" : s.getClassroom().getBuildingName()).isEmpty()
                        ? ""
                        : "（" + s.getClassroom().getBuildingName() + "）")
                        + " 第" + s.getStartWeek() + "-" + s.getEndWeek() + "周")
                .collect(Collectors.joining("；"));
    }

    private String safe(String s) { return s == null ? "" : s; }

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

    private TeachingClassResponse toResp(TeachingClass tc, Course c, String scheduleSummary) {
        return new TeachingClassResponse(
                tc.getId(),
                tc.getClassCode(),
                tc.getCourseId(),
                c == null ? "" : c.getCourseCode(),
                c == null ? "" : c.getCourseName(),
                tc.getTeacherId(),
                tc.getTeacherName(),
                tc.getSemester(),
                tc.getMaxStudents(),
                tc.getCurrentStudents(),
                tc.getCapacity(),
                tc.getCreatedAt(),
                scheduleSummary
        );
    }
}
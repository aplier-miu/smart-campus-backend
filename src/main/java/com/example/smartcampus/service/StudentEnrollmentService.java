package com.example.smartcampus.service;

import com.example.smartcampus.domain.Course;
import com.example.smartcampus.domain.Enrollment;
import com.example.smartcampus.domain.Schedule;
import com.example.smartcampus.domain.TeachingClass;
import com.example.smartcampus.domain.User;
import com.example.smartcampus.dto.student.EnrollRequest;
import com.example.smartcampus.dto.student.MyEnrollmentItemResponse;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.EnrollmentRepository;
import com.example.smartcampus.repository.ScheduleRepository;
import com.example.smartcampus.repository.TeachingClassRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public void enroll(Long studentId, EnrollRequest req) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        if (student.getRole() == null || !"STUDENT".equalsIgnoreCase(student.getRole().name())) {
            throw new BusinessException("只有学生可以选课");
        }

        // 并发保护：对教学班行加悲观写锁
        TeachingClass tc = teachingClassRepository.findByIdForUpdate(req.getTeachingClassId())
                .orElseThrow(() -> new BusinessException("教学班不存在"));

        // 1) 同一教学班重复选
        if (enrollmentRepository.existsByStudent_IdAndTeachingClass_Id(studentId, tc.getId())) {
            throw new BusinessException("你已选过该教学班");
        }

        // 2) 容量校验（锁内检查，防超卖）
        int capacity = tc.getCapacity() == null ? 0 : tc.getCapacity();
        int current = tc.getCurrentStudents() == null ? 0 : tc.getCurrentStudents();
        if (current >= capacity) {
            throw new BusinessException("教学班人数已满");
        }

        // 3) 同课程（同学期）不可重复选不同班
        List<Enrollment> myEnrollments = enrollmentRepository.findByStudent_Id(studentId);
        for (Enrollment en : myEnrollments) {
            TeachingClass selected = en.getTeachingClass();
            if (selected == null) continue;

            boolean sameCourse = Objects.equals(selected.getCourseId(), tc.getCourseId());
            boolean sameSemester = Objects.equals(nvl(selected.getSemester()), nvl(tc.getSemester()));
            if (sameCourse && sameSemester) {
                throw new BusinessException("同一学期同一课程不能重复选不同教学班");
            }
        }

        // 4) 时间冲突检测：weekday + timeSlot + 周次区间重叠
        List<Schedule> targetSchedules = scheduleRepository.findByTeachingClass_Id(tc.getId());
        if (!targetSchedules.isEmpty() && !myEnrollments.isEmpty()) {
            List<Long> selectedClassIds = myEnrollments.stream()
                    .map(e -> e.getTeachingClass() == null ? null : e.getTeachingClass().getId())
                    .filter(Objects::nonNull)
                    .toList();

            List<Schedule> selectedSchedules = selectedClassIds.isEmpty()
                    ? Collections.emptyList()
                    : scheduleRepository.findByTeachingClass_IdIn(selectedClassIds);

            for (Schedule t : targetSchedules) {
                for (Schedule s : selectedSchedules) {
                    if (isConflict(t, s)) {
                        String msg = String.format(
                                "与已选课程时间冲突：%s（周%s %s 第%d-%d周）",
                                nvl(s.getTeachingClass() == null ? "" : s.getTeachingClass().getClassCode()),
                                cnDay(s.getDayOfWeek()),
                                nvl(s.getTimeSlot()),
                                nvlInt(s.getStartWeek()),
                                nvlInt(s.getEndWeek())
                        );
                        throw new BusinessException(msg);
                    }
                }
            }
        }

        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setTeachingClass(tc);
        enrollmentRepository.save(e);

        tc.setCurrentStudents(current + 1);
        teachingClassRepository.save(tc);
    }

    @Transactional
    public void drop(Long studentId, Long teachingClassId) {
        Enrollment e = enrollmentRepository.findByStudent_IdAndTeachingClass_Id(studentId, teachingClassId)
                .orElseThrow(() -> new BusinessException("你未选择该教学班"));

        // 并发保护：退课时也锁教学班，避免并发导致人数异常
        TeachingClass tc = teachingClassRepository.findByIdForUpdate(teachingClassId)
                .orElseThrow(() -> new BusinessException("教学班不存在"));

        enrollmentRepository.delete(e);

        int current = tc.getCurrentStudents() == null ? 0 : tc.getCurrentStudents();
        int next = Math.max(0, current - 1);
        tc.setCurrentStudents(next);
        teachingClassRepository.save(tc);
    }

    public List<MyEnrollmentItemResponse> myEnrollments(Long studentId) {
        List<Enrollment> list = enrollmentRepository.findByStudent_Id(studentId);

        return list.stream().map(e -> {
            TeachingClass tc = e.getTeachingClass();
            if (tc == null) return null;

            Course c = courseRepository.findById(tc.getCourseId()).orElse(null);

            return new MyEnrollmentItemResponse(
                    tc.getId(),
                    tc.getClassCode(),
                    tc.getCourseId(),
                    c == null ? "" : c.getCourseCode(),
                    c == null ? "" : c.getCourseName(),
                    tc.getTeacherName(),
                    tc.getSemester()
            );
        }).filter(Objects::nonNull).toList();
    }

    private boolean isConflict(Schedule a, Schedule b) {
        if (a == null || b == null) return false;
        if (!Objects.equals(a.getDayOfWeek(), b.getDayOfWeek())) return false;
        if (!Objects.equals(nvl(a.getTimeSlot()), nvl(b.getTimeSlot()))) return false;

        int aStart = nvlInt(a.getStartWeek());
        int aEnd = nvlInt(a.getEndWeek());
        int bStart = nvlInt(b.getStartWeek());
        int bEnd = nvlInt(b.getEndWeek());

        return Math.max(aStart, bStart) <= Math.min(aEnd, bEnd);
    }

    private int nvlInt(Integer v) {
        return v == null ? 0 : v;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
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
            default -> String.valueOf(d);
        };
    }
}
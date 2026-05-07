package com.example.smartcampus.service;

import com.example.smartcampus.domain.Classroom;
import com.example.smartcampus.domain.Course;
import com.example.smartcampus.domain.Schedule;
import com.example.smartcampus.domain.TeachingClass;
import com.example.smartcampus.dto.schedule.CreateScheduleRequest;
import com.example.smartcampus.dto.schedule.ScheduleItemResponse;
import com.example.smartcampus.dto.schedule.UpdateScheduleRequest;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.ClassroomRepository;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.ScheduleRepository;
import com.example.smartcampus.repository.TeachingClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;

    public List<ScheduleItemResponse> list(Long teachingClassId) {
        List<Schedule> list = (teachingClassId == null)
                ? scheduleRepository.findAll()
                : scheduleRepository.findByTeachingClass_Id(teachingClassId);
        return list.stream().map(this::toResp).toList();
    }

    @Transactional
    public void create(CreateScheduleRequest req) {
        validateWeeks(req.getStartWeek(), req.getEndWeek());
        String ts = normalizeTimeSlot(req.getTimeSlot());

        TeachingClass tc = teachingClassRepository.findById(req.getTeachingClassId())
                .orElseThrow(() -> new BusinessException("教学班不存在"));

        Classroom room = classroomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new BusinessException("教室不存在"));

        if (Boolean.FALSE.equals(room.getIsAvailable())) {
            throw new BusinessException("教室不可用，无法排课");
        }

        checkConflict(null, tc, room.getId(), req.getDayOfWeek(), ts, req.getStartWeek(), req.getEndWeek());

        Schedule s = new Schedule();
        s.setTeachingClass(tc);
        s.setDayOfWeek(req.getDayOfWeek());
        s.setTimeSlot(ts);
        s.setClassroom(room);
        s.setStartWeek(req.getStartWeek());
        s.setEndWeek(req.getEndWeek());

        scheduleRepository.save(s);
    }

    @Transactional
    public void update(Long id, UpdateScheduleRequest req) {
        validateWeeks(req.getStartWeek(), req.getEndWeek());
        String ts = normalizeTimeSlot(req.getTimeSlot());

        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排课记录不存在"));

        Classroom room = classroomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new BusinessException("教室不存在"));

        if (Boolean.FALSE.equals(room.getIsAvailable())) {
            throw new BusinessException("教室不可用，无法排课");
        }

        TeachingClass tc = s.getTeachingClass();
        checkConflict(id, tc, room.getId(), req.getDayOfWeek(), ts, req.getStartWeek(), req.getEndWeek());

        s.setDayOfWeek(req.getDayOfWeek());
        s.setTimeSlot(ts);
        s.setClassroom(room);
        s.setStartWeek(req.getStartWeek());
        s.setEndWeek(req.getEndWeek());

        scheduleRepository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排课记录不存在"));
        scheduleRepository.delete(s);
    }

    private void validateWeeks(Integer startWeek, Integer endWeek) {
        if (startWeek > endWeek) throw new BusinessException("开始周不能大于结束周");
    }

    private String normalizeTimeSlot(String timeSlot) {
        if (timeSlot == null || timeSlot.trim().isEmpty()) throw new BusinessException("timeSlot不能为空");
        return timeSlot.trim();
    }

    private void checkConflict(Long selfId, TeachingClass tc, Long roomId, Integer dayOfWeek, String timeSlot,
                               Integer startWeek, Integer endWeek) {

        List<Schedule> roomList = scheduleRepository.findByDayOfWeekAndTimeSlotAndClassroom_Id(dayOfWeek, timeSlot, roomId);
        for (Schedule x : roomList) {
            if (isSelf(selfId, x.getId())) continue;
            if (isWeekOverlap(x.getStartWeek(), x.getEndWeek(), startWeek, endWeek)) {
                throw new BusinessException("排课冲突：同一教室同一时段已被占用");
            }
        }

        List<Schedule> teacherList = scheduleRepository.findByDayOfWeekAndTimeSlotAndTeachingClass_TeacherId(dayOfWeek, timeSlot, tc.getTeacherId());
        for (Schedule x : teacherList) {
            if (isSelf(selfId, x.getId())) continue;
            if (isWeekOverlap(x.getStartWeek(), x.getEndWeek(), startWeek, endWeek)) {
                throw new BusinessException("排课冲突：同一教师同一时段已有课程");
            }
        }

        List<Schedule> classList = scheduleRepository.findByDayOfWeekAndTimeSlotAndTeachingClass_Id(dayOfWeek, timeSlot, tc.getId());
        for (Schedule x : classList) {
            if (isSelf(selfId, x.getId())) continue;
            if (isWeekOverlap(x.getStartWeek(), x.getEndWeek(), startWeek, endWeek)) {
                throw new BusinessException("排课冲突：同一教学班同一时段重复排课");
            }
        }
    }

    private boolean isSelf(Long selfId, Long targetId) {
        return selfId != null && selfId.equals(targetId);
    }

    private boolean isWeekOverlap(int a1, int a2, int b1, int b2) {
        return Math.max(a1, b1) <= Math.min(a2, b2);
    }

    private ScheduleItemResponse toResp(Schedule s) {
        TeachingClass tc = s.getTeachingClass();
        Course c = courseRepository.findById(tc.getCourseId()).orElse(null);

        return new ScheduleItemResponse(
                s.getId(),
                tc.getId(),
                tc.getClassCode(),
                tc.getCourseId(),
                c == null ? "" : c.getCourseName(),
                tc.getTeacherId(),
                tc.getTeacherName(),
                s.getDayOfWeek(),
                s.getTimeSlot(),
                s.getClassroom().getId(),
                safe(s.getClassroom().getRoomNumber()),
                safe(s.getClassroom().getBuildingName()),
                s.getStartWeek(),
                s.getEndWeek()
        );
    }

    private String safe(String x) {
        return x == null ? "" : x;
    }
}
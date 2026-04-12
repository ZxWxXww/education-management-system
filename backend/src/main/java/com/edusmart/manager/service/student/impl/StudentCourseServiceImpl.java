package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentCourseClassmateItemDTO;
import com.edusmart.manager.dto.student.StudentCourseDetailDTO;
import com.edusmart.manager.dto.student.StudentCoursePageItemDTO;
import com.edusmart.manager.dto.student.StudentCoursePageQueryDTO;
import com.edusmart.manager.dto.student.StudentCourseScheduleItemDTO;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduExamEntity;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentCourseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentCourseServiceImpl implements StudentCourseService {
    private final EduClassMapper classMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduExamMapper examMapper;
    private final EduExamScoreMapper examScoreMapper;
    private final CurrentUserService currentUserService;

    public StudentCourseServiceImpl(
            EduClassMapper classMapper,
            EduClassStudentMapper classStudentMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            EduClassSessionMapper classSessionMapper,
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduExamMapper examMapper,
            EduExamScoreMapper examScoreMapper,
            CurrentUserService currentUserService
    ) {
        this.classMapper = classMapper;
        this.classStudentMapper = classStudentMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.classSessionMapper = classSessionMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.examMapper = examMapper;
        this.examScoreMapper = examScoreMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentCoursePageItemDTO> pageCourses(StudentCoursePageQueryDTO queryDTO) {
        Long studentProfileId = getCurrentStudentProfileId();
        QueryWrapper<EduClassEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(x -> x.like("class_name", queryDTO.getKeyword()).or().like("class_code", queryDTO.getKeyword()));
        }
        if (queryDTO.getCourseId() != null) {
            wrapper.eq("course_id", queryDTO.getCourseId());
        }
        wrapper.inSql("id", "select class_id from edu_class_student where student_id = " + studentProfileId + " and status = 1");
        wrapper.orderByDesc("id");
        Page<EduClassEntity> page = classMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildPageItems(page.getRecords()));
    }

    @Override
    public StudentCourseDetailDTO getClassDetail(Long classId) {
        EduClassEntity clazz = ensureOwnedClass(classId);
        StudentCoursePageItemDTO base = buildPageItems(Collections.singletonList(clazz)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "班级不存在"));

        StudentCourseDetailDTO detail = new StudentCourseDetailDTO();
        detail.setId(base.getId());
        detail.setClassCode(base.getClassCode());
        detail.setClassName(base.getClassName());
        detail.setCourseId(base.getCourseId());
        detail.setCourseName(base.getCourseName());
        detail.setTeacherName(base.getTeacherName());
        detail.setStartDate(base.getStartDate());
        detail.setEndDate(base.getEndDate());
        detail.setTotalLessons(base.getTotalLessons());
        detail.setCompletedLessons(base.getCompletedLessons());
        detail.setNextLessonTime(base.getNextLessonTime());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setUpdatedAt(base.getUpdatedAt());

        List<EduClassStudentEntity> classStudents = classStudentMapper.selectList(new LambdaQueryWrapper<EduClassStudentEntity>()
                .eq(EduClassStudentEntity::getClassId, classId)
                .eq(EduClassStudentEntity::getStatus, 1));
        List<Long> studentProfileIds = classStudents.stream().map(EduClassStudentEntity::getStudentId).filter(Objects::nonNull).toList();
        Map<Long, EduStudentProfileEntity> studentProfileMap = studentProfileIds.isEmpty()
                ? Collections.emptyMap()
                : studentProfileMapper.selectBatchIds(studentProfileIds).stream()
                .collect(Collectors.toMap(EduStudentProfileEntity::getId, Function.identity(), (left, right) -> right));
        Map<Long, EduUserEntity> studentUserMap = studentProfileMap.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(studentProfileMap.values().stream()
                        .map(EduStudentProfileEntity::getUserId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()).stream()
                .collect(Collectors.toMap(EduUserEntity::getId, Function.identity(), (left, right) -> right));

        List<EduClassSessionEntity> sessions = classSessionMapper.selectList(new LambdaQueryWrapper<EduClassSessionEntity>()
                .eq(EduClassSessionEntity::getClassId, classId)
                .orderByDesc(EduClassSessionEntity::getSessionDate)
                .orderByDesc(EduClassSessionEntity::getStartTime));
        detail.setScheduleList(sessions.stream().limit(10).map(session -> {
            StudentCourseScheduleItemDTO item = new StudentCourseScheduleItemDTO();
            item.setId(session.getId());
            item.setSessionDate(session.getSessionDate());
            item.setStartTime(formatTime(session.getStartTime()));
            item.setEndTime(formatTime(session.getEndTime()));
            item.setStatus(session.getStatus());
            item.setUpdatedAt(session.getUpdatedAt());
            return item;
        }).toList());

        List<EduAttendanceRecordEntity> attendanceRecords = attendanceRecordMapper.selectList(new LambdaQueryWrapper<EduAttendanceRecordEntity>()
                .eq(EduAttendanceRecordEntity::getClassId, classId));
        Map<Long, List<EduAttendanceRecordEntity>> studentAttendanceMap = attendanceRecords.stream()
                .collect(Collectors.groupingBy(EduAttendanceRecordEntity::getStudentId));

        List<EduExamEntity> exams = examMapper.selectList(new LambdaQueryWrapper<EduExamEntity>()
                .eq(EduExamEntity::getClassId, classId));
        List<Long> examIds = exams.stream().map(EduExamEntity::getId).filter(Objects::nonNull).toList();
        List<EduExamScoreEntity> examScores = examIds.isEmpty()
                ? Collections.emptyList()
                : examScoreMapper.selectList(new LambdaQueryWrapper<EduExamScoreEntity>().in(EduExamScoreEntity::getExamId, examIds));
        Map<Long, List<EduExamScoreEntity>> studentScoreMap = examScores.stream()
                .collect(Collectors.groupingBy(EduExamScoreEntity::getStudentId));

        detail.setStudentCount(classStudents.size());
        detail.setAverageAttendanceRate(calculateAttendanceRate(attendanceRecords));
        detail.setAverageScore(calculateAverageScore(examScores));
        detail.setClassmates(classStudents.stream().map(classStudent -> {
            EduStudentProfileEntity profile = studentProfileMap.get(classStudent.getStudentId());
            EduUserEntity user = profile == null ? null : studentUserMap.get(profile.getUserId());
            List<EduAttendanceRecordEntity> studentAttendances = studentAttendanceMap.getOrDefault(classStudent.getStudentId(), Collections.emptyList());
            List<EduExamScoreEntity> studentScores = studentScoreMap.getOrDefault(classStudent.getStudentId(), Collections.emptyList());

            StudentCourseClassmateItemDTO item = new StudentCourseClassmateItemDTO();
            item.setStudentProfileId(classStudent.getStudentId());
            item.setStudentNo(profile == null ? "" : profile.getStudentNo());
            item.setStudentName(user == null ? "" : user.getRealName());
            item.setAttendanceRate(calculateAttendanceRate(studentAttendances));
            item.setLatestScore(resolveLatestScore(studentScores));
            item.setUpdatedAt(resolveLatestUpdate(classStudent, studentAttendances, studentScores));
            return item;
        }).sorted(Comparator.comparing(StudentCourseClassmateItemDTO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))).toList());

        return detail;
    }

    private List<StudentCoursePageItemDTO> buildPageItems(List<EduClassEntity> classes) {
        if (classes == null || classes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> classIds = classes.stream().map(EduClassEntity::getId).filter(Objects::nonNull).toList();
        Map<Long, EduCourseEntity> courseMap = loadCourseMap(classes.stream().map(EduClassEntity::getCourseId).toList());
        Map<Long, EduTeacherProfileEntity> teacherProfileMap = loadTeacherProfileMap(classes.stream().map(EduClassEntity::getHeadTeacherId).toList());
        Map<Long, EduUserEntity> teacherUserMap = loadTeacherUserMap(teacherProfileMap.values());
        List<EduClassSessionEntity> sessions = classIds.isEmpty()
                ? Collections.emptyList()
                : classSessionMapper.selectList(new LambdaQueryWrapper<EduClassSessionEntity>().in(EduClassSessionEntity::getClassId, classIds));

        Map<Long, Integer> totalLessonsMap = sessions.stream()
                .collect(Collectors.groupingBy(EduClassSessionEntity::getClassId, Collectors.summingInt(item -> 1)));
        Map<Long, Integer> completedLessonsMap = sessions.stream()
                .filter(item -> "DONE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.groupingBy(EduClassSessionEntity::getClassId, Collectors.summingInt(item -> 1)));
        Map<Long, String> nextLessonMap = sessions.stream()
                .filter(item -> item.getStartTime() != null)
                .collect(Collectors.groupingBy(EduClassSessionEntity::getClassId, Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                        .filter(item -> item.getStartTime().isAfter(LocalDateTime.now()))
                        .min(Comparator.comparing(EduClassSessionEntity::getStartTime))
                        .map(item -> item.getSessionDate() + " " + formatTime(item.getStartTime()) + " - " + formatTime(item.getEndTime()))
                        .orElse(""))));

        return classes.stream().map(clazz -> {
            StudentCoursePageItemDTO item = new StudentCoursePageItemDTO();
            item.setId(clazz.getId());
            item.setClassCode(clazz.getClassCode());
            item.setClassName(clazz.getClassName());
            item.setCourseId(clazz.getCourseId());
            item.setStartDate(clazz.getStartDate());
            item.setEndDate(clazz.getEndDate());
            item.setTotalLessons(totalLessonsMap.getOrDefault(clazz.getId(), 0));
            item.setCompletedLessons(completedLessonsMap.getOrDefault(clazz.getId(), 0));
            item.setNextLessonTime(nextLessonMap.getOrDefault(clazz.getId(), ""));
            item.setCreatedAt(clazz.getCreatedAt());
            item.setUpdatedAt(clazz.getUpdatedAt());

            EduCourseEntity course = courseMap.get(clazz.getCourseId());
            EduTeacherProfileEntity teacherProfile = teacherProfileMap.get(clazz.getHeadTeacherId());
            EduUserEntity teacherUser = teacherProfile == null ? null : teacherUserMap.get(teacherProfile.getUserId());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            return item;
        }).toList();
    }

    private EduClassEntity ensureOwnedClass(Long classId) {
        EduClassEntity clazz = classMapper.selectById(classId);
        if (clazz == null) {
            throw new ResponseStatusException(NOT_FOUND, "班级不存在");
        }
        Long studentProfileId = getCurrentStudentProfileId();
        long count = classStudentMapper.selectCount(new LambdaQueryWrapper<EduClassStudentEntity>()
                .eq(EduClassStudentEntity::getClassId, classId)
                .eq(EduClassStudentEntity::getStudentId, studentProfileId)
                .eq(EduClassStudentEntity::getStatus, 1));
        if (count <= 0) {
            throw new ResponseStatusException(NOT_FOUND, "班级不存在");
        }
        return clazz;
    }

    private Map<Long, EduCourseEntity> loadCourseMap(List<Long> courseIds) {
        List<Long> ids = courseIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, Function.identity(), (left, right) -> right));
    }

    private Map<Long, EduTeacherProfileEntity> loadTeacherProfileMap(List<Long> teacherProfileIds) {
        List<Long> ids = teacherProfileIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return teacherProfileMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(EduTeacherProfileEntity::getId, Function.identity(), (left, right) -> right));
    }

    private Map<Long, EduUserEntity> loadTeacherUserMap(Iterable<EduTeacherProfileEntity> teacherProfiles) {
        List<Long> userIds = java.util.stream.StreamSupport.stream(teacherProfiles.spliterator(), false)
                .map(EduTeacherProfileEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(EduUserEntity::getId, Function.identity(), (left, right) -> right));
    }

    private BigDecimal calculateAttendanceRate(List<EduAttendanceRecordEntity> attendanceRecords) {
        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long presentCount = attendanceRecords.stream()
                .filter(item -> item != null && !"ABSENT".equalsIgnoreCase(item.getStatus()))
                .count();
        return BigDecimal.valueOf(presentCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(attendanceRecords.size()), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageScore(List<EduExamScoreEntity> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = scores.stream().map(EduExamScoreEntity::getScore).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = scores.stream().map(EduExamScoreEntity::getScore).filter(Objects::nonNull).count();
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveLatestScore(List<EduExamScoreEntity> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return scores.stream()
                .max(Comparator.comparing(EduExamScoreEntity::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(EduExamScoreEntity::getScore)
                .orElse(BigDecimal.ZERO);
    }

    private LocalDateTime resolveLatestUpdate(EduClassStudentEntity classStudent,
                                              List<EduAttendanceRecordEntity> attendanceRecords,
                                              List<EduExamScoreEntity> scores) {
        LocalDateTime latest = classStudent.getUpdatedAt();
        for (EduAttendanceRecordEntity record : attendanceRecords) {
            if (record != null && record.getUpdatedAt() != null && (latest == null || record.getUpdatedAt().isAfter(latest))) {
                latest = record.getUpdatedAt();
            }
        }
        for (EduExamScoreEntity score : scores) {
            if (score != null && score.getUpdatedAt() != null && (latest == null || score.getUpdatedAt().isAfter(latest))) {
                latest = score.getUpdatedAt();
            }
        }
        return latest;
    }

    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.toLocalTime().toString();
    }

    private Long getCurrentStudentProfileId() {
        EduStudentProfileEntity profile = studentProfileMapper.selectOne(
                new QueryWrapper<EduStudentProfileEntity>().eq("user_id", currentUserService.getCurrentUserId())
        );
        if (profile == null) {
            throw new ResponseStatusException(NOT_FOUND, "学生档案不存在");
        }
        return profile.getId();
    }
}

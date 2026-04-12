package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherClassDetailDTO;
import com.edusmart.manager.dto.teacher.TeacherClassPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherClassPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSaveDTO;
import com.edusmart.manager.dto.teacher.TeacherClassSessionItemDTO;
import com.edusmart.manager.dto.teacher.TeacherClassStudentItemDTO;
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
import com.edusmart.manager.service.teacher.TeacherClassService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class TeacherClassServiceImpl implements TeacherClassService {
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduExamMapper examMapper;
    private final EduExamScoreMapper examScoreMapper;

    public TeacherClassServiceImpl(
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            EduClassStudentMapper classStudentMapper,
            EduClassSessionMapper classSessionMapper,
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduExamMapper examMapper,
            EduExamScoreMapper examScoreMapper
    ) {
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.classStudentMapper = classStudentMapper;
        this.classSessionMapper = classSessionMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.examMapper = examMapper;
        this.examScoreMapper = examScoreMapper;
    }

    @Override
    public PageData<TeacherClassPageItemDTO> page(TeacherClassPageQueryDTO queryDTO) {
        QueryWrapper<EduClassEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(x -> x.like("class_code", queryDTO.getKeyword()).or().like("class_name", queryDTO.getKeyword()));
        }
        if (queryDTO.getCourseId() != null) {
            wrapper.eq("course_id", queryDTO.getCourseId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        wrapper.orderByDesc("id");
        Page<EduClassEntity> page = classMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildPageItems(page.getRecords()));
    }

    @Override
    public TeacherClassDetailDTO getById(Long id) {
        EduClassEntity clazz = classMapper.selectById(id);
        if (clazz == null) {
            throw new ResponseStatusException(NOT_FOUND, "班级不存在");
        }
        return buildDetail(clazz);
    }

    @Override
    public Long create(TeacherClassSaveDTO dto) {
        EduClassEntity entity = new EduClassEntity();
        entity.setClassCode(dto.getClassCode());
        entity.setClassName(dto.getClassName());
        entity.setCourseId(dto.getCourseId());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus());
        classMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, TeacherClassSaveDTO dto) {
        EduClassEntity entity = classMapper.selectById(id);
        if (entity == null) {
            return;
        }
        entity.setClassCode(dto.getClassCode());
        entity.setClassName(dto.getClassName());
        entity.setCourseId(dto.getCourseId());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus());
        classMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        classMapper.deleteById(id);
    }

    private List<TeacherClassPageItemDTO> buildPageItems(List<EduClassEntity> classRecords) {
        if (classRecords == null || classRecords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> classIds = classRecords.stream().map(EduClassEntity::getId).filter(Objects::nonNull).toList();
        Map<Long, EduCourseEntity> courseMap = loadCourseMap(classRecords.stream().map(EduClassEntity::getCourseId).toList());
        Map<Long, EduTeacherProfileEntity> teacherProfileMap = loadTeacherProfileMap(classRecords.stream().map(EduClassEntity::getHeadTeacherId).toList());
        Map<Long, EduUserEntity> teacherUserMap = loadTeacherUserMap(teacherProfileMap.values());
        List<EduClassStudentEntity> classStudents = loadClassStudents(classIds);
        List<EduClassSessionEntity> classSessions = loadClassSessions(classIds);
        List<EduAttendanceRecordEntity> attendanceRecords = loadAttendanceRecords(classIds);

        Map<Long, Integer> studentCountMap = classStudents.stream()
                .collect(Collectors.groupingBy(EduClassStudentEntity::getClassId,
                        Collectors.collectingAndThen(Collectors.mapping(EduClassStudentEntity::getStudentId, Collectors.toSet()), Set::size)));
        Map<Long, Integer> sessionCountMap = classSessions.stream()
                .collect(Collectors.groupingBy(EduClassSessionEntity::getClassId, Collectors.summingInt(item -> 1)));
        Map<Long, BigDecimal> attendanceRateMap = buildAttendanceRateMap(attendanceRecords);

        return classRecords.stream().map(clazz -> {
            TeacherClassPageItemDTO item = new TeacherClassPageItemDTO();
            item.setId(clazz.getId());
            item.setClassCode(clazz.getClassCode());
            item.setClassName(clazz.getClassName());
            item.setCourseId(clazz.getCourseId());
            item.setHeadTeacherId(clazz.getHeadTeacherId());
            item.setStatus(clazz.getStatus());
            item.setStartDate(clazz.getStartDate());
            item.setEndDate(clazz.getEndDate());
            item.setCreatedAt(clazz.getCreatedAt());
            item.setUpdatedAt(clazz.getUpdatedAt());
            item.setStudentCount(studentCountMap.getOrDefault(clazz.getId(), 0));
            item.setSessionCount(sessionCountMap.getOrDefault(clazz.getId(), 0));
            item.setAttendanceRate(attendanceRateMap.getOrDefault(clazz.getId(), BigDecimal.ZERO));

            EduCourseEntity course = courseMap.get(clazz.getCourseId());
            EduTeacherProfileEntity teacherProfile = teacherProfileMap.get(clazz.getHeadTeacherId());
            EduUserEntity teacherUser = teacherProfile == null ? null : teacherUserMap.get(teacherProfile.getUserId());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setHeadTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            return item;
        }).toList();
    }

    private TeacherClassDetailDTO buildDetail(EduClassEntity clazz) {
        TeacherClassPageItemDTO base = buildPageItems(Collections.singletonList(clazz)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "班级不存在"));

        TeacherClassDetailDTO detail = new TeacherClassDetailDTO();
        detail.setId(base.getId());
        detail.setClassCode(base.getClassCode());
        detail.setClassName(base.getClassName());
        detail.setCourseId(base.getCourseId());
        detail.setCourseName(base.getCourseName());
        detail.setHeadTeacherId(base.getHeadTeacherId());
        detail.setHeadTeacherName(base.getHeadTeacherName());
        detail.setStatus(base.getStatus());
        detail.setStartDate(base.getStartDate());
        detail.setEndDate(base.getEndDate());
        detail.setStudentCount(base.getStudentCount());
        detail.setSessionCount(base.getSessionCount());
        detail.setAttendanceRate(base.getAttendanceRate());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setUpdatedAt(base.getUpdatedAt());

        EduTeacherProfileEntity headTeacherProfile = clazz.getHeadTeacherId() == null ? null : teacherProfileMapper.selectById(clazz.getHeadTeacherId());
        detail.setHeadTeacherTitle(headTeacherProfile == null ? "" : headTeacherProfile.getTitle());
        detail.setSubject(headTeacherProfile == null ? "" : headTeacherProfile.getSubject());

        List<EduClassSessionEntity> sessions = classSessionMapper.selectList(new LambdaQueryWrapper<EduClassSessionEntity>()
                .eq(EduClassSessionEntity::getClassId, clazz.getId())
                .orderByDesc(EduClassSessionEntity::getSessionDate)
                .orderByDesc(EduClassSessionEntity::getId));
        detail.setRecentSessions(sessions.stream().limit(8).map(session -> {
            TeacherClassSessionItemDTO item = new TeacherClassSessionItemDTO();
            item.setId(session.getId());
            item.setSessionTitle(detail.getCourseName());
            item.setSessionDate(session.getSessionDate());
            item.setStartTime(formatDateTime(session.getStartTime()));
            item.setEndTime(formatDateTime(session.getEndTime()));
            item.setStatus(session.getStatus());
            item.setRemark("");
            item.setCreatedAt(session.getCreatedAt());
            item.setUpdatedAt(session.getUpdatedAt());
            return item;
        }).toList());

        List<EduClassStudentEntity> classStudents = classStudentMapper.selectList(new LambdaQueryWrapper<EduClassStudentEntity>()
                .eq(EduClassStudentEntity::getClassId, clazz.getId())
                .orderByDesc(EduClassStudentEntity::getCreatedAt));
        List<Long> studentProfileIds = classStudents.stream()
                .map(EduClassStudentEntity::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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

        List<EduAttendanceRecordEntity> attendanceRecords = attendanceRecordMapper.selectList(new LambdaQueryWrapper<EduAttendanceRecordEntity>()
                .eq(EduAttendanceRecordEntity::getClassId, clazz.getId()));
        Map<Long, List<EduAttendanceRecordEntity>> studentAttendanceMap = attendanceRecords.stream()
                .collect(Collectors.groupingBy(EduAttendanceRecordEntity::getStudentId));

        List<EduExamEntity> exams = examMapper.selectList(new LambdaQueryWrapper<EduExamEntity>()
                .eq(EduExamEntity::getClassId, clazz.getId()));
        List<Long> examIds = exams.stream().map(EduExamEntity::getId).filter(Objects::nonNull).toList();
        List<EduExamScoreEntity> examScores = examIds.isEmpty()
                ? Collections.emptyList()
                : examScoreMapper.selectList(new LambdaQueryWrapper<EduExamScoreEntity>().in(EduExamScoreEntity::getExamId, examIds));
        Map<Long, List<EduExamScoreEntity>> studentScoreMap = examScores.stream()
                .collect(Collectors.groupingBy(EduExamScoreEntity::getStudentId));

        detail.setAverageScore(calculateAverageScore(examScores));
        detail.setStudents(classStudents.stream().map(classStudent -> {
            EduStudentProfileEntity profile = studentProfileMap.get(classStudent.getStudentId());
            EduUserEntity user = profile == null ? null : studentUserMap.get(profile.getUserId());
            List<EduAttendanceRecordEntity> studentAttendances = studentAttendanceMap.getOrDefault(classStudent.getStudentId(), Collections.emptyList());
            List<EduExamScoreEntity> studentScores = studentScoreMap.getOrDefault(classStudent.getStudentId(), Collections.emptyList());

            TeacherClassStudentItemDTO item = new TeacherClassStudentItemDTO();
            item.setStudentProfileId(classStudent.getStudentId());
            item.setUserId(profile == null ? null : profile.getUserId());
            item.setStudentNo(profile == null ? "" : profile.getStudentNo());
            item.setStudentName(user == null ? "" : user.getRealName());
            item.setGrade(profile == null ? "" : profile.getGrade());
            item.setJoinedAt(classStudent.getCreatedAt());
            item.setAttendanceCount(studentAttendances.size());
            item.setAttendanceRate(calculateAttendanceRate(studentAttendances));
            item.setAverageScore(calculateAverageScore(studentScores));
            item.setEnrollmentStatus(Objects.equals(classStudent.getStatus(), 1) ? "在班" : "离班");
            return item;
        }).sorted(Comparator.comparing(TeacherClassStudentItemDTO::getJoinedAt, Comparator.nullsLast(Comparator.reverseOrder()))).toList());
        return detail;
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

    private List<EduClassStudentEntity> loadClassStudents(List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }
        return classStudentMapper.selectList(new LambdaQueryWrapper<EduClassStudentEntity>()
                .in(EduClassStudentEntity::getClassId, classIds)
                .eq(EduClassStudentEntity::getStatus, 1));
    }

    private List<EduClassSessionEntity> loadClassSessions(List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }
        return classSessionMapper.selectList(new LambdaQueryWrapper<EduClassSessionEntity>()
                .in(EduClassSessionEntity::getClassId, classIds));
    }

    private List<EduAttendanceRecordEntity> loadAttendanceRecords(List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }
        return attendanceRecordMapper.selectList(new LambdaQueryWrapper<EduAttendanceRecordEntity>()
                .in(EduAttendanceRecordEntity::getClassId, classIds));
    }

    private Map<Long, BigDecimal> buildAttendanceRateMap(List<EduAttendanceRecordEntity> attendanceRecords) {
        return attendanceRecords.stream()
                .collect(Collectors.groupingBy(EduAttendanceRecordEntity::getClassId, Collectors.collectingAndThen(Collectors.toList(), this::calculateAttendanceRate)));
    }

    private BigDecimal calculateAttendanceRate(List<EduAttendanceRecordEntity> attendanceRecords) {
        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long validCount = attendanceRecords.stream()
                .filter(item -> item != null && !"ABSENT".equalsIgnoreCase(item.getStatus()))
                .count();
        return BigDecimal.valueOf(validCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(attendanceRecords.size()), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageScore(List<EduExamScoreEntity> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = scores.stream()
                .map(EduExamScoreEntity::getScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = scores.stream().map(EduExamScoreEntity::getScore).filter(Objects::nonNull).count();
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.toLocalTime().toString();
    }
}

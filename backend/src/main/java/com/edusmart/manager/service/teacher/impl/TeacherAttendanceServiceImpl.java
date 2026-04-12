package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.StatusLabelMapper;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchTaskPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchTaskPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchUpdateDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceSaveDTO;
import com.edusmart.manager.dto.teacher.TeacherBatchAttendanceSaveDTO;
import com.edusmart.manager.entity.EduAttendanceBatchTaskEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduAttendanceBatchTaskMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.HourPackageLedgerService;
import com.edusmart.manager.service.teacher.TeacherAttendanceService;
import com.edusmart.manager.service.teacher.TeacherScopeGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TeacherAttendanceServiceImpl implements TeacherAttendanceService {
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduAttendanceBatchTaskMapper batchTaskMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final TeacherScopeGuard teacherScopeGuard;
    private final HourPackageLedgerService hourPackageLedgerService;
    private static final DateTimeFormatter SESSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TeacherAttendanceServiceImpl(
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduAttendanceBatchTaskMapper batchTaskMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduClassSessionMapper classSessionMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService,
            TeacherScopeGuard teacherScopeGuard,
            HourPackageLedgerService hourPackageLedgerService
    ) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.batchTaskMapper = batchTaskMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.classSessionMapper = classSessionMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
        this.teacherScopeGuard = teacherScopeGuard;
        this.hourPackageLedgerService = hourPackageLedgerService;
    }

    @Override
    public PageData<TeacherAttendancePageItemDTO> page(TeacherAttendancePageQueryDTO queryDTO) {
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (teacherProfile == null) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduAttendanceRecordEntity> w = new QueryWrapper<EduAttendanceRecordEntity>().eq("teacher_id", teacherProfile.getId());
        if (queryDTO.getClassId() != null) w.eq("class_id", queryDTO.getClassId());
        if (queryDTO.getStudentId() != null) w.eq("student_id", queryDTO.getStudentId());
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) w.eq("status", queryDTO.getStatus());
        if (queryDTO.getAttendanceDate() != null) w.eq("attendance_date", queryDTO.getAttendanceDate());
        w.orderByDesc("id");
        Page<EduAttendanceRecordEntity> p = attendanceRecordMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), buildAttendanceItems(p.getRecords()));
    }

    @Override
    public TeacherAttendancePageItemDTO getById(Long id) {
        EduAttendanceRecordEntity record = teacherScopeGuard.requireOwnedAttendanceRecord(id);
        return buildAttendanceItems(Collections.singletonList(record)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "考勤记录不存在"));
    }

    @Override
    @Transactional
    public Long create(TeacherAttendanceSaveDTO dto) {
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        validateAttendanceWriteScope(dto.getClassId(), dto.getSessionId(), dto.getStudentId());
        EduAttendanceRecordEntity e = new EduAttendanceRecordEntity();
        fill(e, dto, teacherProfile.getId());
        attendanceRecordMapper.insert(e);
        hourPackageLedgerService.refreshAttendanceDeduction(e);
        return e.getId();
    }

    @Override
    @Transactional
    public void update(Long id, TeacherAttendanceSaveDTO dto) {
        EduAttendanceRecordEntity e = teacherScopeGuard.requireOwnedAttendanceRecord(id);
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        validateAttendanceWriteScope(dto.getClassId(), dto.getSessionId(), dto.getStudentId());
        fill(e, dto, teacherProfile.getId());
        attendanceRecordMapper.updateById(e);
        hourPackageLedgerService.refreshAttendanceDeduction(e);
    }

    @Override
    @Transactional
    public void batchUpdate(TeacherAttendanceBatchUpdateDTO dto) {
        Long teacherProfileId = teacherScopeGuard.requireCurrentTeacherProfileId();
        List<EduAttendanceRecordEntity> records = attendanceRecordMapper.selectBatchIds(dto.getRecordIds());
        if (records.size() != dto.getRecordIds().size()) {
            throw new ResponseStatusException(NOT_FOUND, "考勤记录不存在");
        }
        for (EduAttendanceRecordEntity record : records) {
            if (!Objects.equals(record.getTeacherId(), teacherProfileId)) {
                throw new ResponseStatusException(NOT_FOUND, "考勤记录不存在");
            }
            record.setStatus(dto.getStatus());
            record.setRemark(dto.getRemark());
            attendanceRecordMapper.updateById(record);
            hourPackageLedgerService.refreshAttendanceDeduction(record);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EduAttendanceRecordEntity record = teacherScopeGuard.requireOwnedAttendanceRecord(id);
        hourPackageLedgerService.rollbackAttendanceDeduction(record);
        attendanceRecordMapper.deleteById(id);
    }

    @Override
    public Long createBatchTask(TeacherBatchAttendanceSaveDTO dto) {
        EduTeacherProfileEntity teacherProfile = teacherScopeGuard.requireCurrentTeacherProfile();
        teacherScopeGuard.requireOwnedClass(dto.getClassId());
        EduAttendanceBatchTaskEntity e = new EduAttendanceBatchTaskEntity();
        e.setOperatorTeacherId(teacherProfile.getId());
        e.setClassId(dto.getClassId());
        e.setAttendanceDate(dto.getAttendanceDate());
        e.setTaskStatus(dto.getTaskStatus());
        e.setRemark(dto.getRemark());
        batchTaskMapper.insert(e);
        return e.getId();
    }

    @Override
    public TeacherAttendanceBatchTaskPageItemDTO getBatchTask(Long id) {
        EduAttendanceBatchTaskEntity task = batchTaskMapper.selectById(id);
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (task == null || teacherProfile == null || !Objects.equals(task.getOperatorTeacherId(), teacherProfile.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "批任务不存在");
        }
        return buildBatchTaskItems(Collections.singletonList(task)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "批任务不存在"));
    }

    @Override
    public PageData<TeacherAttendanceBatchTaskPageItemDTO> pageBatchTasks(TeacherAttendanceBatchTaskPageQueryDTO queryDTO) {
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (teacherProfile == null) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduAttendanceBatchTaskEntity> wrapper = new QueryWrapper<EduAttendanceBatchTaskEntity>()
                .eq("operator_teacher_id", teacherProfile.getId())
                .orderByDesc("id");
        Page<EduAttendanceBatchTaskEntity> page = batchTaskMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildBatchTaskItems(page.getRecords()));
    }

    private void fill(EduAttendanceRecordEntity e, TeacherAttendanceSaveDTO dto, Long teacherId) {
        e.setClassId(dto.getClassId());
        e.setSessionId(dto.getSessionId());
        e.setStudentId(dto.getStudentId());
        e.setTeacherId(teacherId);
        e.setAttendanceDate(dto.getAttendanceDate());
        e.setStatus(dto.getStatus());
        e.setRemark(dto.getRemark());
    }

    private void validateAttendanceWriteScope(Long classId, Long sessionId, Long studentId) {
        teacherScopeGuard.requireOwnedClass(classId);
        if (sessionId != null) {
            EduClassSessionEntity session = teacherScopeGuard.requireOwnedSession(sessionId);
            if (!Objects.equals(session.getClassId(), classId)) {
                throw new ResponseStatusException(NOT_FOUND, "课节不存在");
            }
        }
        teacherScopeGuard.requireStudentInClass(studentId, classId);
    }

    private EduTeacherProfileEntity getCurrentTeacherProfile() {
        return teacherProfileMapper.selectOne(new QueryWrapper<EduTeacherProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
    }

    private List<TeacherAttendancePageItemDTO> buildAttendanceItems(List<EduAttendanceRecordEntity> records) {
        if (records == null || records.isEmpty()) return Collections.emptyList();
        Map<Long, EduStudentProfileEntity> studentMap = studentProfileMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getStudentId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduStudentProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        classMap.values().stream().map(EduClassEntity::getCourseId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduClassSessionEntity> sessionMap = classSessionMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getSessionId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduClassSessionEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getTeacherId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(
                        studentMap.values().stream().map(EduStudentProfileEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));
        userMap.putAll(userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right)));

        return records.stream().map(record -> {
            EduStudentProfileEntity student = studentMap.get(record.getStudentId());
            EduUserEntity studentUser = student == null ? null : userMap.get(student.getUserId());
            EduClassEntity clazz = classMap.get(record.getClassId());
            EduCourseEntity course = clazz == null ? null : courseMap.get(clazz.getCourseId());
            EduTeacherProfileEntity teacher = teacherMap.get(record.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());
            EduClassSessionEntity session = sessionMap.get(record.getSessionId());
            TeacherAttendancePageItemDTO item = new TeacherAttendancePageItemDTO();
            item.setId(record.getId());
            item.setClassId(record.getClassId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setStudentId(record.getStudentId());
            item.setStudentName(studentUser == null ? "" : studentUser.getRealName());
            item.setStudentNo(student == null ? "" : student.getStudentNo());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setAttendanceDate(record.getAttendanceDate());
            item.setAttendanceStatus(StatusLabelMapper.attendanceStatusLabel(record.getStatus()));
            item.setRemark(record.getRemark());
            item.setSessionTime(StatusLabelMapper.formatSessionTime(
                    session == null ? null : session.getStartTime(),
                    session == null ? null : session.getEndTime()
            ));
            item.setCreatedAt(record.getCreatedAt());
            item.setUpdatedAt(record.getUpdatedAt());
            return item;
        }).collect(Collectors.toList());
    }

    private List<TeacherAttendanceBatchTaskPageItemDTO> buildBatchTaskItems(List<EduAttendanceBatchTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) return Collections.emptyList();
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        tasks.stream().map(EduAttendanceBatchTaskEntity::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        tasks.stream().map(EduAttendanceBatchTaskEntity::getOperatorTeacherId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));

        return tasks.stream().map(task -> {
            EduClassEntity clazz = classMap.get(task.getClassId());
            EduTeacherProfileEntity teacher = teacherMap.get(task.getOperatorTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());
            TeacherAttendanceBatchTaskPageItemDTO item = new TeacherAttendanceBatchTaskPageItemDTO();
            item.setId(task.getId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setAttendanceDate(task.getAttendanceDate());
            item.setTaskStatus(task.getTaskStatus());
            item.setRemark(task.getRemark());
            item.setOperatorName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setCreatedAt(task.getCreatedAt());
            item.setUpdatedAt(task.getUpdatedAt());
            return item;
        }).collect(Collectors.toList());
    }

    private String mapAttendanceStatus(String status) {
        if ("LATE".equalsIgnoreCase(status)) return "迟到";
        if ("LEAVE".equalsIgnoreCase(status)) return "请假";
        if ("ABSENT".equalsIgnoreCase(status)) return "缺勤";
        if ("PRESENT".equalsIgnoreCase(status)) return "正常";
        return status == null ? "" : status;
    }

    private String formatSessionTime(EduClassSessionEntity session) {
        if (session == null || session.getStartTime() == null || session.getEndTime() == null) return "待排课";
        return session.getStartTime().format(SESSION_TIME_FORMATTER) + "-" + session.getEndTime().format(SESSION_TIME_FORMATTER);
    }
}

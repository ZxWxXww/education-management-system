package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.StatusLabelMapper;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.student.StudentAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageItemDTO;
import com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentAttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentAttendanceServiceImpl implements StudentAttendanceService {
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduAttendanceExceptionMapper attendanceExceptionMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;

    public StudentAttendanceServiceImpl(
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduAttendanceExceptionMapper attendanceExceptionMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduClassSessionMapper classSessionMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService
    ) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceExceptionMapper = attendanceExceptionMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.classSessionMapper = classSessionMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentAttendancePageItemDTO> pageAttendance(StudentAttendancePageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceRecordEntity> wrapper = new QueryWrapper<EduAttendanceRecordEntity>()
                .eq("student_id", getCurrentStudentProfileId());
        if (queryDTO.getClassId() != null) {
            wrapper.eq("class_id", queryDTO.getClassId());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        if (queryDTO.getAttendanceDate() != null) {
            wrapper.eq("attendance_date", queryDTO.getAttendanceDate());
        }
        wrapper.orderByDesc("id");
        Page<EduAttendanceRecordEntity> page = attendanceRecordMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildAttendanceItems(page.getRecords()));
    }

    @Override
    public StudentAttendancePageItemDTO getAttendance(Long id) {
        EduAttendanceRecordEntity record = attendanceRecordMapper.selectById(id);
        if (record == null || !Objects.equals(record.getStudentId(), getCurrentStudentProfileId())) {
            throw new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        return buildAttendanceItems(Collections.singletonList(record)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u8bb0\u5f55\u4e0d\u5b58\u5728"));
    }

    @Override
    public PageData<StudentAttendanceExceptionPageItemDTO> pageExceptions(StudentAttendanceExceptionPageQueryDTO queryDTO) {
        Long studentProfileId = getCurrentStudentProfileId();
        List<Long> recordIds = attendanceRecordMapper.selectList(new QueryWrapper<EduAttendanceRecordEntity>().eq("student_id", studentProfileId))
                .stream()
                .map(EduAttendanceRecordEntity::getId)
                .toList();
        if (recordIds.isEmpty()) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        QueryWrapper<EduAttendanceExceptionEntity> wrapper = new QueryWrapper<EduAttendanceExceptionEntity>()
                .in("attendance_record_id", recordIds);
        if (queryDTO.getAttendanceRecordId() != null) {
            wrapper.eq("attendance_record_id", queryDTO.getAttendanceRecordId());
        }
        if (queryDTO.getIsResolved() != null) {
            wrapper.eq("is_resolved", queryDTO.getIsResolved());
        }
        if (queryDTO.getExceptionType() != null && !queryDTO.getExceptionType().isBlank()) {
            wrapper.eq("exception_type", queryDTO.getExceptionType());
        }
        wrapper.orderByDesc("id");
        Page<EduAttendanceExceptionEntity> page = attendanceExceptionMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildExceptionItems(page.getRecords()));
    }

    @Override
    public StudentAttendanceExceptionPageItemDTO getException(Long id) {
        EduAttendanceExceptionEntity exception = attendanceExceptionMapper.selectById(id);
        if (exception == null) {
            throw new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u5f02\u5e38\u4e0d\u5b58\u5728");
        }
        EduAttendanceRecordEntity record = attendanceRecordMapper.selectById(exception.getAttendanceRecordId());
        if (record == null || !Objects.equals(record.getStudentId(), getCurrentStudentProfileId())) {
            throw new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u5f02\u5e38\u4e0d\u5b58\u5728");
        }
        return buildExceptionItems(Collections.singletonList(exception)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u5f02\u5e38\u4e0d\u5b58\u5728"));
    }

    private Long getCurrentStudentProfileId() {
        EduStudentProfileEntity profile = studentProfileMapper.selectOne(new QueryWrapper<EduStudentProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
        if (profile == null) {
            throw new ResponseStatusException(NOT_FOUND, "\u5b66\u751f\u6863\u6848\u4e0d\u5b58\u5728");
        }
        return profile.getId();
    }

    private List<StudentAttendancePageItemDTO> buildAttendanceItems(List<EduAttendanceRecordEntity> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getClassId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        classMap.values().stream().map(EduClassEntity::getCourseId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduClassSessionEntity> sessionMap = classSessionMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getSessionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduClassSessionEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        records.stream().map(EduAttendanceRecordEntity::getTeacherId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> teacherUserMap = userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));

        return records.stream().map(record -> {
            EduClassEntity clazz = classMap.get(record.getClassId());
            EduCourseEntity course = clazz == null ? null : courseMap.get(clazz.getCourseId());
            EduClassSessionEntity session = sessionMap.get(record.getSessionId());
            EduTeacherProfileEntity teacher = teacherMap.get(record.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : teacherUserMap.get(teacher.getUserId());

            StudentAttendancePageItemDTO item = new StudentAttendancePageItemDTO();
            item.setId(record.getId());
            item.setClassId(record.getClassId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setSessionId(record.getSessionId());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setTeacherId(record.getTeacherId());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setAttendanceDate(record.getAttendanceDate());
            item.setWeekDay(StatusLabelMapper.weekDayLabel(record.getAttendanceDate()));
            item.setAttendanceStatus(StatusLabelMapper.attendanceStatusLabel(record.getStatus()));
            item.setAttendanceStatusValue(record.getStatus());
            item.setSessionTime(StatusLabelMapper.formatSessionTime(
                    session == null ? null : session.getStartTime(),
                    session == null ? null : session.getEndTime()
            ));
            item.setRemark(record.getRemark());
            item.setCreatedAt(record.getCreatedAt());
            item.setUpdatedAt(record.getUpdatedAt());
            return item;
        }).toList();
    }

    private List<StudentAttendanceExceptionPageItemDTO> buildExceptionItems(List<EduAttendanceExceptionEntity> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduAttendanceRecordEntity> recordMap = attendanceRecordMapper.selectBatchIds(
                        exceptions.stream().map(EduAttendanceExceptionEntity::getAttendanceRecordId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduAttendanceRecordEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getClassId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        classMap.values().stream().map(EduClassEntity::getCourseId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduClassSessionEntity> sessionMap = classSessionMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getSessionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduClassSessionEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getTeacherId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduUserEntity> teacherUserMap = userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));

        return exceptions.stream().map(exception -> {
            EduAttendanceRecordEntity record = recordMap.get(exception.getAttendanceRecordId());
            EduClassEntity clazz = record == null ? null : classMap.get(record.getClassId());
            EduCourseEntity course = clazz == null ? null : courseMap.get(clazz.getCourseId());
            EduClassSessionEntity session = record == null ? null : sessionMap.get(record.getSessionId());
            EduTeacherProfileEntity teacher = record == null ? null : teacherMap.get(record.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : teacherUserMap.get(teacher.getUserId());

            StudentAttendanceExceptionPageItemDTO item = new StudentAttendanceExceptionPageItemDTO();
            item.setId(exception.getId());
            item.setAttendanceDate(record == null ? null : record.getAttendanceDate());
            item.setWeekDay(StatusLabelMapper.weekDayLabel(record == null ? null : record.getAttendanceDate()));
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setClassTime(StatusLabelMapper.formatSessionTime(
                    session == null ? null : session.getStartTime(),
                    session == null ? null : session.getEndTime()
            ));
            item.setAbnormalType(StatusLabelMapper.attendanceExceptionTypeLabel(exception.getExceptionType()));
            item.setStatus(StatusLabelMapper.studentAttendanceExceptionStatusLabel(exception.getIsResolved()));
            item.setCheckTime(StatusLabelMapper.clockTimeLabel(session == null ? null : session.getStartTime()));
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setReason(record == null ? "" : record.getRemark());
            item.setHandleNote(exception.getResolveRemark());
            item.setCreatedAt(exception.getCreatedAt());
            item.setUpdatedAt(exception.getUpdatedAt());
            return item;
        }).toList();
    }
}

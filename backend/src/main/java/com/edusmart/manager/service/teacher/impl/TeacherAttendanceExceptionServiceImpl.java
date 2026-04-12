package com.edusmart.manager.service.teacher.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.StatusLabelMapper;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO;
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
import com.edusmart.manager.service.teacher.TeacherAttendanceExceptionService;
import com.edusmart.manager.service.teacher.TeacherScopeGuard;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TeacherAttendanceExceptionServiceImpl implements TeacherAttendanceExceptionService {
    private final EduAttendanceExceptionMapper mapper;
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final TeacherScopeGuard teacherScopeGuard;

    public TeacherAttendanceExceptionServiceImpl(
            EduAttendanceExceptionMapper mapper,
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduClassSessionMapper classSessionMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService,
            TeacherScopeGuard teacherScopeGuard
    ) {
        this.mapper = mapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.classSessionMapper = classSessionMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
        this.teacherScopeGuard = teacherScopeGuard;
    }

    @Override
    public PageData<TeacherAttendanceExceptionPageItemDTO> page(TeacherAttendanceExceptionPageQueryDTO queryDTO) {
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (teacherProfile == null) {
            return new PageData<>(queryDTO.getCurrent(), queryDTO.getSize(), 0, Collections.emptyList());
        }
        List<Long> recordIds = attendanceRecordMapper.selectList(new QueryWrapper<EduAttendanceRecordEntity>().eq("teacher_id", teacherProfile.getId()))
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
        Page<EduAttendanceExceptionEntity> page = mapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildItems(page.getRecords()));
    }

    @Override
    public TeacherAttendanceExceptionPageItemDTO getById(Long id) {
        EduAttendanceExceptionEntity exception = mapper.selectById(id);
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (exception == null || teacherProfile == null) {
            throw new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u5f02\u5e38\u4e0d\u5b58\u5728");
        }
        EduAttendanceRecordEntity record = attendanceRecordMapper.selectById(exception.getAttendanceRecordId());
        if (record == null || !Objects.equals(record.getTeacherId(), teacherProfile.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u5f02\u5e38\u4e0d\u5b58\u5728");
        }
        return buildItems(Collections.singletonList(exception)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u5f02\u5e38\u4e0d\u5b58\u5728"));
    }

    @Override
    public Long create(TeacherAttendanceExceptionSaveDTO dto) {
        ensureTeacherOwnsRecord(dto.getAttendanceRecordId());
        EduAttendanceExceptionEntity entity = new EduAttendanceExceptionEntity();
        fill(entity, dto);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, TeacherAttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity entity = teacherScopeGuard.requireOwnedAttendanceException(id);
        teacherScopeGuard.requireOwnedAttendanceRecord(entity.getAttendanceRecordId());
        ensureTeacherOwnsRecord(dto.getAttendanceRecordId());
        fill(entity, dto);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        teacherScopeGuard.requireOwnedAttendanceException(id);
        mapper.deleteById(id);
    }

    private void fill(EduAttendanceExceptionEntity entity, TeacherAttendanceExceptionSaveDTO dto) {
        entity.setAttendanceRecordId(dto.getAttendanceRecordId());
        entity.setExceptionType(dto.getExceptionType());
        entity.setSeverity(dto.getSeverity());
        entity.setIsResolved(dto.getIsResolved());
        entity.setResolvedBy(dto.getIsResolved() != null && dto.getIsResolved() == 1 ? currentUserService.getCurrentUserId() : null);
        entity.setResolveRemark(dto.getResolveRemark());
        if (dto.getIsResolved() != null && dto.getIsResolved() == 1) {
            entity.setResolvedAt(LocalDateTime.now());
        }
    }

    private void ensureTeacherOwnsRecord(Long attendanceRecordId) {
        EduAttendanceRecordEntity record = attendanceRecordMapper.selectById(attendanceRecordId);
        EduTeacherProfileEntity teacherProfile = getCurrentTeacherProfile();
        if (record == null || teacherProfile == null || !Objects.equals(record.getTeacherId(), teacherProfile.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "\u8003\u52e4\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
    }

    private EduTeacherProfileEntity getCurrentTeacherProfile() {
        return teacherProfileMapper.selectOne(new QueryWrapper<EduTeacherProfileEntity>()
                .eq("user_id", currentUserService.getCurrentUserId()));
    }

    private List<TeacherAttendanceExceptionPageItemDTO> buildItems(List<EduAttendanceExceptionEntity> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduAttendanceRecordEntity> recordMap = attendanceRecordMapper.selectBatchIds(
                        exceptions.stream().map(EduAttendanceExceptionEntity::getAttendanceRecordId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduAttendanceRecordEntity::getId, item -> item, (left, right) -> right));
        Map<Long, EduStudentProfileEntity> studentMap = studentProfileMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getStudentId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduStudentProfileEntity::getId, item -> item, (left, right) -> right));
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
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(
                        studentMap.values().stream().map(EduStudentProfileEntity::getUserId).filter(Objects::nonNull).toList())
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right));
        userMap.putAll(userMapper.selectBatchIds(
                        teacherMap.values().stream().map(EduTeacherProfileEntity::getUserId).filter(Objects::nonNull).toList())
                .stream().collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> right)));

        return exceptions.stream().map(exception -> {
            EduAttendanceRecordEntity record = recordMap.get(exception.getAttendanceRecordId());
            EduStudentProfileEntity student = record == null ? null : studentMap.get(record.getStudentId());
            EduUserEntity studentUser = student == null ? null : userMap.get(student.getUserId());
            EduClassEntity clazz = record == null ? null : classMap.get(record.getClassId());
            EduCourseEntity course = clazz == null ? null : courseMap.get(clazz.getCourseId());
            EduTeacherProfileEntity teacher = record == null ? null : teacherMap.get(record.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());
            EduClassSessionEntity session = record == null ? null : sessionMap.get(record.getSessionId());

            TeacherAttendanceExceptionPageItemDTO item = new TeacherAttendanceExceptionPageItemDTO();
            item.setId(exception.getId());
            item.setAttendanceRecordId(exception.getAttendanceRecordId());
            item.setStudentName(studentUser == null ? "" : studentUser.getRealName());
            item.setStudentNo(student == null ? "" : student.getStudentNo());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setAttendanceDate(record == null ? null : record.getAttendanceDate());
            item.setSessionTime(StatusLabelMapper.formatSessionTime(
                    session == null ? null : session.getStartTime(),
                    session == null ? null : session.getEndTime()
            ));
            item.setExceptionType(StatusLabelMapper.attendanceExceptionTypeLabel(exception.getExceptionType()));
            item.setSeverity(exception.getSeverity());
            item.setResolved(exception.getIsResolved() != null && exception.getIsResolved() == 1);
            item.setHandleStatus(StatusLabelMapper.attendanceHandleStatusLabel(exception.getIsResolved()));
            item.setRemark(record == null ? "" : record.getRemark());
            item.setResolveRemark(exception.getResolveRemark());
            item.setCreatedAt(exception.getCreatedAt());
            item.setUpdatedAt(exception.getUpdatedAt());
            return item;
        }).toList();
    }
}

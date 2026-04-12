package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.StatusLabelMapper;
import com.edusmart.manager.dto.admin.AdminAttendanceExceptionPageItemDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionPageQueryDTO;
import com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO;
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
import com.edusmart.manager.service.admin.AdminAttendanceExceptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminAttendanceExceptionServiceImpl implements AdminAttendanceExceptionService {
    private final EduAttendanceExceptionMapper attendanceExceptionMapper;
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduClassMapper classMapper;
    private final EduCourseMapper courseMapper;
    private final EduClassSessionMapper classSessionMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;

    public AdminAttendanceExceptionServiceImpl(
            EduAttendanceExceptionMapper attendanceExceptionMapper,
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduClassMapper classMapper,
            EduCourseMapper courseMapper,
            EduClassSessionMapper classSessionMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService
    ) {
        this.attendanceExceptionMapper = attendanceExceptionMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.classMapper = classMapper;
        this.courseMapper = courseMapper;
        this.classSessionMapper = classSessionMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<AdminAttendanceExceptionPageItemDTO> page(AttendanceExceptionPageQueryDTO queryDTO) {
        QueryWrapper<EduAttendanceExceptionEntity> wrapper = new QueryWrapper<>();
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
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), buildItems(page.getRecords()));
    }

    @Override
    public AdminAttendanceExceptionPageItemDTO getById(Long id) {
        EduAttendanceExceptionEntity entity = attendanceExceptionMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return buildItems(Collections.singletonList(entity)).stream().findFirst().orElse(null);
    }

    @Override
    public Long create(AttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity entity = new EduAttendanceExceptionEntity();
        fill(entity, dto);
        attendanceExceptionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, AttendanceExceptionSaveDTO dto) {
        EduAttendanceExceptionEntity old = attendanceExceptionMapper.selectById(id);
        if (old == null) {
            return;
        }
        fill(old, dto);
        attendanceExceptionMapper.updateById(old);
    }

    @Override
    public void delete(Long id) {
        attendanceExceptionMapper.deleteById(id);
    }

    private List<AdminAttendanceExceptionPageItemDTO> buildItems(List<EduAttendanceExceptionEntity> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduAttendanceRecordEntity> recordMap = attendanceRecordMapper.selectBatchIds(
                        exceptions.stream().map(EduAttendanceExceptionEntity::getAttendanceRecordId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduAttendanceRecordEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduStudentProfileEntity> studentMap = studentProfileMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getStudentId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduStudentProfileEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getClassId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduCourseEntity> courseMap = courseMapper.selectBatchIds(
                        classMap.values().stream().map(EduClassEntity::getCourseId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduClassSessionEntity> sessionMap = classSessionMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getSessionId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduClassSessionEntity::getId, item -> item, (left, right) -> left));
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(
                        recordMap.values().stream().map(EduAttendanceRecordEntity::getTeacherId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> left));

        List<Long> userIds = studentMap.values().stream()
                .map(EduStudentProfileEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        userIds.addAll(teacherMap.values().stream()
                .map(EduTeacherProfileEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        userIds.addAll(exceptions.stream()
                .map(EduAttendanceExceptionEntity::getResolvedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(userIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> left));

        return exceptions.stream().map(exception -> {
            EduAttendanceRecordEntity record = recordMap.get(exception.getAttendanceRecordId());
            EduStudentProfileEntity student = record == null ? null : studentMap.get(record.getStudentId());
            EduUserEntity studentUser = student == null ? null : userMap.get(student.getUserId());
            EduClassEntity clazz = record == null ? null : classMap.get(record.getClassId());
            EduCourseEntity course = clazz == null ? null : courseMap.get(clazz.getCourseId());
            EduTeacherProfileEntity teacher = record == null ? null : teacherMap.get(record.getTeacherId());
            EduUserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());
            EduClassSessionEntity session = record == null ? null : sessionMap.get(record.getSessionId());
            EduUserEntity resolvedUser = exception.getResolvedBy() == null ? null : userMap.get(exception.getResolvedBy());

            AdminAttendanceExceptionPageItemDTO item = new AdminAttendanceExceptionPageItemDTO();
            item.setId(exception.getId());
            item.setAttendanceRecordId(exception.getAttendanceRecordId());
            item.setStudentName(studentUser == null ? "" : studentUser.getRealName());
            item.setStudentNo(student == null ? "" : student.getStudentNo());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            item.setTeacherName(teacherUser == null ? "" : teacherUser.getRealName());
            item.setCourseName(course == null ? "" : course.getCourseName());
            item.setCheckTime(session == null ? null : session.getStartTime());
            item.setAbnormalType(StatusLabelMapper.attendanceExceptionTypeLabel(exception.getExceptionType()));
            item.setAbnormalTypeValue(exception.getExceptionType());
            item.setSeverity(exception.getSeverity());
            item.setIsResolved(exception.getIsResolved() == null ? 0 : exception.getIsResolved());
            item.setHandleStatus(StatusLabelMapper.attendanceHandleStatusLabel(exception.getIsResolved()));
            item.setRemark(record == null ? "" : record.getRemark());
            item.setResolveRemark(exception.getResolveRemark());
            item.setResolvedByName(resolvedUser == null ? "" : resolvedUser.getRealName());
            item.setResolvedAt(exception.getResolvedAt());
            item.setCreatedAt(exception.getCreatedAt());
            item.setUpdatedAt(exception.getUpdatedAt());
            return item;
        }).toList();
    }

    private void fill(EduAttendanceExceptionEntity entity, AttendanceExceptionSaveDTO dto) {
        entity.setAttendanceRecordId(dto.getAttendanceRecordId());
        entity.setExceptionType(dto.getExceptionType());
        entity.setSeverity(dto.getSeverity());
        entity.setIsResolved(dto.getIsResolved() == null ? 0 : dto.getIsResolved());
        entity.setResolvedBy(entity.getIsResolved() != null && entity.getIsResolved() == 1 ? currentUserService.getCurrentUserId() : null);
        entity.setResolveRemark(dto.getResolveRemark());
        if (entity.getIsResolved() != null && entity.getIsResolved() == 1) {
            entity.setResolvedAt(LocalDateTime.now());
        } else {
            entity.setResolvedAt(null);
        }
    }
}

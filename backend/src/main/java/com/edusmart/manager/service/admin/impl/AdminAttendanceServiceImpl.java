package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.dto.admin.AdminAttendanceClassOverviewDTO;
import com.edusmart.manager.dto.admin.AdminAttendanceFocusAlertDTO;
import com.edusmart.manager.dto.admin.AdminAttendanceOverviewDTO;
import com.edusmart.manager.dto.admin.AdminAttendanceTrendPointDTO;
import com.edusmart.manager.entity.EduAttendanceExceptionEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduClassStudentEntity;
import com.edusmart.manager.entity.EduTeacherProfileEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduAttendanceExceptionMapper;
import com.edusmart.manager.mapper.EduAttendanceRecordMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduClassStudentMapper;
import com.edusmart.manager.mapper.EduTeacherProfileMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.service.admin.AdminAttendanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminAttendanceServiceImpl implements AdminAttendanceService {
    private final EduAttendanceRecordMapper attendanceRecordMapper;
    private final EduAttendanceExceptionMapper attendanceExceptionMapper;
    private final EduClassMapper classMapper;
    private final EduClassStudentMapper classStudentMapper;
    private final EduTeacherProfileMapper teacherProfileMapper;
    private final EduUserMapper userMapper;

    public AdminAttendanceServiceImpl(
            EduAttendanceRecordMapper attendanceRecordMapper,
            EduAttendanceExceptionMapper attendanceExceptionMapper,
            EduClassMapper classMapper,
            EduClassStudentMapper classStudentMapper,
            EduTeacherProfileMapper teacherProfileMapper,
            EduUserMapper userMapper
    ) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceExceptionMapper = attendanceExceptionMapper;
        this.classMapper = classMapper;
        this.classStudentMapper = classStudentMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.userMapper = userMapper;
    }

    @Override
    public AdminAttendanceOverviewDTO getOverview() {
        List<EduAttendanceRecordEntity> allRecords = attendanceRecordMapper.selectList(new QueryWrapper<EduAttendanceRecordEntity>().orderByDesc("attendance_date", "id"));
        LocalDate anchorDate = resolveAnchorDate(allRecords);
        LocalDate startDate = anchorDate.minusDays(6);
        List<EduAttendanceRecordEntity> recentRecords = allRecords.stream()
                .filter(item -> item.getAttendanceDate() != null && !item.getAttendanceDate().isBefore(startDate) && !item.getAttendanceDate().isAfter(anchorDate))
                .collect(Collectors.toList());

        List<EduAttendanceExceptionEntity> allExceptions = attendanceExceptionMapper.selectList(new QueryWrapper<EduAttendanceExceptionEntity>().orderByDesc("id"));
        Map<Long, EduAttendanceRecordEntity> recordMap = allRecords.stream()
                .collect(Collectors.toMap(EduAttendanceRecordEntity::getId, item -> item, (left, right) -> left));
        List<EduAttendanceExceptionEntity> recentExceptions = allExceptions.stream()
                .filter(item -> {
                    EduAttendanceRecordEntity record = recordMap.get(item.getAttendanceRecordId());
                    return record != null
                            && record.getAttendanceDate() != null
                            && !record.getAttendanceDate().isBefore(startDate)
                            && !record.getAttendanceDate().isAfter(anchorDate);
                })
                .collect(Collectors.toList());

        List<AdminAttendanceClassOverviewDTO> classOverview = buildClassOverview(recentRecords);

        AdminAttendanceOverviewDTO overview = new AdminAttendanceOverviewDTO();
        overview.setTotalAttendanceCount((long) recentRecords.size());
        overview.setPunctualRate(calculateRate(countByStatus(recentRecords, "PRESENT"), recentRecords.size()));
        overview.setLeaveCount(countByStatus(recentRecords, "LEAVE"));
        overview.setExceptionCount((long) recentExceptions.size());
        overview.setTrend(buildTrend(anchorDate, recentRecords));
        overview.setClassOverview(classOverview);
        overview.setFocusAlerts(buildFocusAlerts(classOverview, recentExceptions));
        return overview;
    }

    private List<AdminAttendanceTrendPointDTO> buildTrend(LocalDate anchorDate, List<EduAttendanceRecordEntity> recentRecords) {
        Map<LocalDate, List<EduAttendanceRecordEntity>> recordGroup = recentRecords.stream()
                .filter(item -> item.getAttendanceDate() != null)
                .collect(Collectors.groupingBy(EduAttendanceRecordEntity::getAttendanceDate));
        List<AdminAttendanceTrendPointDTO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = anchorDate.minusDays(i);
            List<EduAttendanceRecordEntity> dayRecords = recordGroup.getOrDefault(date, Collections.emptyList());
            AdminAttendanceTrendPointDTO item = new AdminAttendanceTrendPointDTO();
            item.setAttendanceDate(date);
            item.setLabel(mapWeekDay(date));
            item.setPunctualRate(calculateRate(countByStatus(dayRecords, "PRESENT"), dayRecords.size()));
            result.add(item);
        }
        return result;
    }

    private List<AdminAttendanceClassOverviewDTO> buildClassOverview(List<EduAttendanceRecordEntity> recentRecords) {
        if (recentRecords.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<EduAttendanceRecordEntity>> recordGroup = recentRecords.stream()
                .filter(item -> item.getClassId() != null)
                .collect(Collectors.groupingBy(EduAttendanceRecordEntity::getClassId, LinkedHashMap::new, Collectors.toList()));
        List<Long> classIds = new ArrayList<>(recordGroup.keySet());
        Map<Long, EduClassEntity> classMap = classMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> left));
        Map<Long, Long> classStudentCountMap = classStudentMapper.selectList(new QueryWrapper<EduClassStudentEntity>()
                        .in("class_id", classIds)
                        .eq("status", 1))
                .stream()
                .collect(Collectors.groupingBy(EduClassStudentEntity::getClassId, Collectors.counting()));

        List<Long> teacherProfileIds = classMap.values().stream()
                .map(EduClassEntity::getHeadTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, EduTeacherProfileEntity> teacherMap = teacherProfileMapper.selectBatchIds(teacherProfileIds).stream()
                .collect(Collectors.toMap(EduTeacherProfileEntity::getId, item -> item, (left, right) -> left));
        List<Long> userIds = teacherMap.values().stream()
                .map(EduTeacherProfileEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, EduUserEntity> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> left));

        return recordGroup.entrySet().stream()
                .map(entry -> {
                    Long classId = entry.getKey();
                    List<EduAttendanceRecordEntity> rows = entry.getValue();
                    EduClassEntity clazz = classMap.get(classId);
                    EduTeacherProfileEntity teacherProfile = clazz == null ? null : teacherMap.get(clazz.getHeadTeacherId());
                    EduUserEntity teacherUser = teacherProfile == null ? null : userMap.get(teacherProfile.getUserId());
                    long shouldAttend = classStudentCountMap.getOrDefault(classId, (long) rows.size());
                    long actualAttend = rows.stream().filter(item -> isAttended(item.getStatus())).count();
                    long abnormalCount = rows.stream().filter(item -> !isPresent(item.getStatus())).count();

                    AdminAttendanceClassOverviewDTO item = new AdminAttendanceClassOverviewDTO();
                    item.setClassId(classId);
                    item.setClassName(clazz == null ? ("班级 " + classId) : clazz.getClassName());
                    item.setTeacherName(teacherUser == null ? "-" : teacherUser.getRealName());
                    item.setShouldAttend(shouldAttend);
                    item.setActualAttend(actualAttend);
                    item.setNormalRate(calculateRate(actualAttend, shouldAttend));
                    item.setAbnormalCount(abnormalCount);
                    return item;
                })
                .sorted(Comparator.comparing(AdminAttendanceClassOverviewDTO::getNormalRate))
                .collect(Collectors.toList());
    }

    private List<AdminAttendanceFocusAlertDTO> buildFocusAlerts(
            List<AdminAttendanceClassOverviewDTO> classOverview,
            List<EduAttendanceExceptionEntity> recentExceptions
    ) {
        List<AdminAttendanceFocusAlertDTO> result = new ArrayList<>();

        classOverview.stream()
                .filter(item -> item.getNormalRate() != null && item.getNormalRate() < 95)
                .limit(2)
                .forEach(item -> result.add(createAlert(item.getNormalRate() < 90 ? "高" : "中",
                        item.getClassName() + " 最近 7 日到课率为 " + formatRate(item.getNormalRate()) + "%")));

        long unresolvedCount = recentExceptions.stream()
                .filter(item -> item.getIsResolved() == null || item.getIsResolved() == 0)
                .count();
        if (unresolvedCount > 0) {
            result.add(createAlert(unresolvedCount >= 5 ? "高" : "低", "当前仍有 " + unresolvedCount + " 条异常考勤待处理"));
        }

        if (result.isEmpty()) {
            result.add(createAlert("低", "最近 7 日暂无明显考勤异常趋势"));
        }

        return result.stream().limit(3).collect(Collectors.toList());
    }

    private AdminAttendanceFocusAlertDTO createAlert(String level, String text) {
        AdminAttendanceFocusAlertDTO item = new AdminAttendanceFocusAlertDTO();
        item.setLevel(level);
        item.setText(text);
        return item;
    }

    private LocalDate resolveAnchorDate(List<EduAttendanceRecordEntity> allRecords) {
        return allRecords.stream()
                .map(EduAttendanceRecordEntity::getAttendanceDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }

    private long countByStatus(List<EduAttendanceRecordEntity> records, String status) {
        return records.stream().filter(item -> status.equalsIgnoreCase(item.getStatus())).count();
    }

    private boolean isAttended(String status) {
        return "PRESENT".equalsIgnoreCase(status) || "LATE".equalsIgnoreCase(status);
    }

    private boolean isPresent(String status) {
        return "PRESENT".equalsIgnoreCase(status);
    }

    private double calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((numerator * 10000D) / denominator) / 100D;
    }

    private String formatRate(Double value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.2f", value);
    }

    private String mapWeekDay(LocalDate date) {
        return switch (date.getDayOfWeek().getValue()) {
            case 1 -> "周一";
            case 2 -> "周二";
            case 3 -> "周三";
            case 4 -> "周四";
            case 5 -> "周五";
            case 6 -> "周六";
            case 7 -> "周日";
            default -> "";
        };
    }
}
